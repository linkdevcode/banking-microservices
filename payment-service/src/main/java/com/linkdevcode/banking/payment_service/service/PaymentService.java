package com.linkdevcode.banking.payment_service.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import com.linkdevcode.banking.payment_service.client.user_service.UserClient;
import com.linkdevcode.banking.payment_service.client.user_service.request.BalanceUpdateRequest;
import com.linkdevcode.banking.payment_service.client.user_service.response.UserLookupResponse;
import com.linkdevcode.banking.payment_service.entity.Transaction;
import com.linkdevcode.banking.payment_service.event.TransactionCompletedEvent;
import com.linkdevcode.banking.payment_service.model.request.TransferRequest;
import com.linkdevcode.banking.payment_service.model.response.TransactionHistoryResponse;
import com.linkdevcode.banking.payment_service.model.response.TransferResponse;
import com.linkdevcode.banking.payment_service.producer.KafkaProducerService;
import com.linkdevcode.banking.payment_service.repository.TransactionRepository;
import org.springframework.data.domain.Pageable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Service class responsible for processing money transfers and managing transactions.
 * It acts as an Orchestrator for distributed transactions (calling User Service).
 */
@Service
@Slf4j
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final UserClient userClient;
    private final KafkaProducerService kafkaProducerService;

    /**
     * Constructor for Dependency Injection.
     */
    public PaymentService(
        TransactionRepository transactionRepository, 
        UserClient userClient,
        KafkaProducerService kafkaProducerService) {
        this.transactionRepository = transactionRepository;
        this.userClient = userClient;
        this.kafkaProducerService = kafkaProducerService;
    }

    /**
     * Executes the money transfer flow (Deduct -> Add -> Publish Event).
     * This method is transactional to ensure local database integrity (transaction status).
     * @param senderId The ID of the user initiating the transfer.
     * @param request The transfer details (recipient, amount, message).
     * @return TransferResponse with status.
     */
    @Transactional
    public TransferResponse processTransfer(Long senderId, TransferRequest request) {
        log.info("Starting transfer from {} to {} for amount {}", senderId, request.getRecipientId(), request.getAmount());

        // Initialize Transaction record (PENDING)
        Transaction transaction = new Transaction();
        transaction.setSenderId(senderId);
        transaction.setRecipientId(request.getRecipientId());
        transaction.setAmount(request.getAmount());
        transaction.setStatus("PENDING");
        transaction.setMessage(request.getMessage());
        transaction = transactionRepository.save(transaction); // Save to get the transaction ID

        try {
            // Basic Validations
            if (senderId.equals(request.getRecipientId())) {
                throw new IllegalArgumentException("Cannot transfer money to yourself.");
            }
            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                 throw new IllegalArgumentException("Transfer amount must be positive.");
            }

            // --- DISTRIBUTED TRANSACTION STEPS (Saga Orchestration) ---
            
            // Deduct money from Sender
            log.info("Attempting to deduct balance from sender: {}", senderId);
            BalanceUpdateRequest deductRequest = new BalanceUpdateRequest();
            deductRequest.setAmount(request.getAmount());
            
            // This call throws HttpClientErrorException (4xx) on validation failure
            userClient.deductBalance(senderId, deductRequest);

            // Add money to Recipient
            log.info("Attempting to add balance to recipient: {}", request.getRecipientId());
            BalanceUpdateRequest addRequest = new BalanceUpdateRequest();
            addRequest.setAmount(request.getAmount());
            
            userClient.addBalance(request.getRecipientId(), addRequest);
            
            // --- SUCCESS ---
            
            // Update Transaction status to SUCCESS
            transaction.setStatus("SUCCESS");
            transactionRepository.save(transaction);
            
            // Produce Kafka Event for History Service (Eventual Consistency)
            kafkaProducerService.sendTransactionCompletedEvent(
                new TransactionCompletedEvent(
                    transaction.getId(),
                    transaction.getSenderId(),
                    transaction.getRecipientId(),
                    transaction.getAmount(),
                    transaction.getMessage(),
                    transaction.getStatus(),
                    LocalDateTime.now()
                )
            );

            log.info("Transfer {} completed successfully and event published.", transaction.getId());
            return new TransferResponse("SUCCESS", transaction.getId(), "Transfer completed.");

        } catch (HttpClientErrorException e) {
            String reason = "User Service Error: " + e.getResponseBodyAsString();
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                 reason = "Validation/Funds Error: " + e.getResponseBodyAsString();
            }
            log.error("Transfer failed due to User Service error for transaction {}: {}", transaction.getId(), reason);
            
            // Update local status to FAILED
            transaction.setStatus("FAILED");
            transaction.setMessage(reason);
            transactionRepository.save(transaction); 
            
            // Propagate the exception to the controller
            throw new ResponseStatusException(e.getStatusCode(), reason);
            
        } catch (Exception e) {
            log.error("Transfer failed due to unexpected error for transaction {}: {}", transaction.getId(), e.getMessage());
            
            // Update local status to FAILED
            transaction.setStatus("FAILED");
            transaction.setMessage("Unexpected error during transfer: " + e.getMessage());
            transactionRepository.save(transaction);
            
            // Propagate a generic 500 status
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error during transfer.");
        }
    }

    /**
     * Retrieves transaction history for a user and enriches it with user names.
     * @param id The ID of the user requesting the history.
     * @param pageable Pagination and sorting criteria.
     * @return Paginated list of TransactionHistoryResponse.
     */
    public Page<TransactionHistoryResponse> getHistory(Long id, Pageable pageable) {
        
        // Query local transactions where the user is either sender or recipient
        Page<Transaction> transactionPage = transactionRepository
            .findBySenderIdOrRecipientId(id, id, pageable);

        // Identify all unique User IDs (Sender and Recipient) involved in the page
        Set<Long> involvedIds = transactionPage.getContent().stream()
            .flatMap(t -> Set.of(t.getSenderId(), t.getRecipientId()).stream())
            .collect(Collectors.toSet());
        
        // Remove the current user's ID, as we might already have their name or can handle it separately
        involvedIds.remove(id); 

        // Data Enrichment: Call User Service to get profile data (Names)
        Map<Long, String> userNamesMap = fetchUserNames(involvedIds);
        
        // Map Entity to DTO and set names
        return transactionPage.map(transaction -> {
            TransactionHistoryResponse dto = TransactionHistoryResponse.fromEntity(transaction);
            
            // Get Sender Name
            String senderName = userNamesMap.getOrDefault(transaction.getSenderId(), "User ID " + transaction.getSenderId());
            dto.setSenderName(senderName);

            // Get Recipient Name
            String recipientName = userNamesMap.getOrDefault(transaction.getRecipientId(), "User ID " + transaction.getRecipientId());
            dto.setRecipientName(recipientName);

            return dto;
        });
    }

    /**
     * Helper method to call User Service for a set of User IDs (Enrichment).
     */
    @SuppressWarnings("null")
    private Map<Long, String> fetchUserNames(Set<Long> Ids) {
        Map<Long, String> namesMap = new HashMap<>();
        
        // TODO: The current approach loops through IDs, which is inefficient. 
        
        for (Long id : Ids) {
            try {
                // Get profile
                ResponseEntity<UserLookupResponse> response = userClient.getUserProfileForInternal(id);
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    namesMap.put(id, response.getBody().getFullName());
                } else {
                    namesMap.put(id, "User ID " + id + " (Not Found)");
                }
            } catch (Exception e) {
                // Handle connection errors or service downtime
                log.warn("Failed to fetch profile for user ID {}: {}", id, e.getMessage());
                namesMap.put(id, "User ID " + id + " (Lookup Failed)");
            }
        }
        return namesMap;
    }
}