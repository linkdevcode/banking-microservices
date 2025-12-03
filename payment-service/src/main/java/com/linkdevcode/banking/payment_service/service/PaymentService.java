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
import com.linkdevcode.banking.payment_service.model.request.TransferRequest;
import com.linkdevcode.banking.payment_service.model.response.TransactionHistoryResponse;
import com.linkdevcode.banking.payment_service.model.response.TransferResponse;
import com.linkdevcode.banking.payment_service.repository.TransactionRepository;
import org.springframework.data.domain.Pageable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import java.math.BigDecimal;

/**
 * Service class responsible for processing money transfers and managing transactions.
 * It acts as an Orchestrator for distributed transactions (calling User Service).
 */
@Service
@Slf4j
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final UserClient userClient; // Feign Client dependency

    public PaymentService(TransactionRepository transactionRepository, UserClient userClient) {
        this.transactionRepository = transactionRepository;
        this.userClient = userClient;
    }

    /**
     * Executes the money transfer flow (Flow 2).
     * This method is transactional to ensure local database integrity (transaction status).
     * @param senderId The ID of the user initiating the transfer.
     * @param request The transfer details (recipient, amount, message).
     * @return TransferResponse with status.
     */
    @Transactional
    public TransferResponse processTransfer(Long senderId, TransferRequest request) {
        log.info("Starting transfer from {} to {} for amount {}", senderId, request.getRecipientId(), request.getAmount());

        // 1. Initialize Transaction record (PENDING)
        Transaction transaction = new Transaction();
        transaction.setSenderId(senderId);
        transaction.setRecipientId(request.getRecipientId());
        transaction.setAmount(request.getAmount());
        transaction.setStatus("PENDING");
        transaction.setMessage(request.getMessage());
        transaction = transactionRepository.save(transaction); // Save to get the transaction ID

        try {
            // Check if sender and recipient are the same (simple validation)
            if (senderId.equals(request.getRecipientId())) {
                throw new IllegalArgumentException("Cannot transfer money to yourself.");
            }
            
            // Check if amount is positive
            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                 throw new IllegalArgumentException("Transfer amount must be positive.");
            }

            // --- DISTRIBUTED TRANSACTION STEPS (Calling User Service) ---

            // 2. Check Sender Balance (Internal API 1)
            // The check for insufficient funds will happen within the deductBalance call,
            // but getting the balance explicitly can be used for logging/debugging.
            
            // 3. Deduct money from Sender (Internal API 2)
            log.info("Attempting to deduct balance from sender: {}", senderId);
            BalanceUpdateRequest deductRequest = new BalanceUpdateRequest();
            deductRequest.setAmount(request.getAmount());
            
            // This call will throw an exception (HttpClientErrorException) if the balance is insufficient
            // or the user is not found, which will be caught below.
            userClient.deductBalance(senderId, deductRequest);

            // 4. Add money to Recipient (Internal API 3)
            log.info("Attempting to add balance to recipient: {}", request.getRecipientId());
            BalanceUpdateRequest addRequest = new BalanceUpdateRequest();
            addRequest.setAmount(request.getAmount());
            
            userClient.addBalance(request.getRecipientId(), addRequest);
            
            // --- SUCCESS ---
            
            // 5. Update Transaction status to SUCCESS
            transaction.setStatus("SUCCESS");
            transactionRepository.save(transaction);
            
            log.info("Transfer {} completed successfully.", transaction.getId());
            return new TransferResponse("SUCCESS", transaction.getId(), "Transfer completed.");

        } catch (HttpClientErrorException e) {
            // Catch HTTP errors from User Service (4xx series, e.g., 404 Not Found, 400 Bad Request)
            String reason = "User Service Error: " + e.getResponseBodyAsString();
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                 reason = "Validation/Funds Error: " + e.getResponseBodyAsString();
            }
            log.error("Transfer failed due to User Service error for transaction {}: {}", transaction.getId(), reason);
            
            transaction.setStatus("FAILED");
            transaction.setMessage(reason);
            transactionRepository.save(transaction); // Save FAILED status
            
            throw new ResponseStatusException(e.getStatusCode(), reason);
        } catch (Exception e) {
            // Catch any other exceptions (e.g., connection timeout, general runtime errors)
            log.error("Transfer failed due to unexpected error for transaction {}: {}", transaction.getId(), e.getMessage());
            
            transaction.setStatus("FAILED");
            transaction.setMessage("Unexpected error during transfer: " + e.getMessage());
            transactionRepository.save(transaction); // Save FAILED status
            
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error during transfer.");
        }
    }

    /**
     * Retrieves transaction history for a user and enriches it with user names (Flow 3).
     * @param userId The ID of the user requesting the history.
     * @param pageable Pagination and sorting criteria.
     * @return Paginated list of TransactionHistoryResponse.
     */
    public Page<TransactionHistoryResponse> getHistory(Long userId, Pageable pageable) {
        
        // 1. Query local transactions where the user is either sender or recipient
        Page<Transaction> transactionPage = transactionRepository
            .findBySenderIdOrRecipientId(userId, userId, pageable);

        // 2. Identify all unique User IDs (Sender and Recipient) involved in the page
        Set<Long> involvedUserIds = transactionPage.getContent().stream()
            .flatMap(t -> Set.of(t.getSenderId(), t.getRecipientId()).stream())
            .collect(Collectors.toSet());

        // Loại bỏ chính userId của người đang xem để tối ưu (tên của họ có thể đã biết)
        // involvedUserIds.remove(userId); 

        // 3. Data Enrichment: Call User Service to get profile data (Names)
        // Map để lưu trữ tên: Key=UserId, Value=FullName
        Map<Long, String> userNamesMap = fetchUserNames(involvedUserIds);
        
        // Thêm tên của chính user đang request vào map (giả sử ta đã biết hoặc có thể tự lookup)
        // Trong một hệ thống thực tế, thông tin của user đang login có thể được lấy từ token
        // Để đơn giản, ta chỉ dựa vào kết quả lookup từ User Service

        // 4. Map Entity to DTO and set names
        return transactionPage.map(transaction -> {
            TransactionHistoryResponse dto = TransactionHistoryResponse.fromEntity(transaction);
            
            // Lấy tên người gửi
            String senderName = userNamesMap.getOrDefault(transaction.getSenderId(), "Unknown User");
            dto.setSenderName(senderName);

            // Lấy tên người nhận
            String recipientName = userNamesMap.getOrDefault(transaction.getRecipientId(), "Unknown User");
            dto.setRecipientName(recipientName);

            return dto;
        });
    }

    /**
     * Helper method to call User Service for a set of User IDs.
     */
    @SuppressWarnings("null")
    private Map<Long, String> fetchUserNames(Set<Long> userIds) {
        Map<Long, String> namesMap = new HashMap<>();
        
        // IMPORTANT NOTE: Calling Feign Client in a loop (many HTTP calls) is INEFFICIENT.
        // In a real production environment, User Service should provide a BULK lookup API 
        // (e.g., POST /internal/users/lookup with a list of IDs)
        
        for (Long id : userIds) {
            try {
                // Call Internal API to get profile
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