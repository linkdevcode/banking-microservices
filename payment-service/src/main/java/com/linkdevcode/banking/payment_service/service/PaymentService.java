package com.linkdevcode.banking.payment_service.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import com.linkdevcode.banking.payment_service.client.user_service.UserClient;
import com.linkdevcode.banking.payment_service.client.user_service.request.BalanceUpdateRequest;
import com.linkdevcode.banking.payment_service.entity.OutboxEvent;
import com.linkdevcode.banking.payment_service.entity.Transaction;
import com.linkdevcode.banking.payment_service.enumeration.EOutboxStatus;
import com.linkdevcode.banking.payment_service.enumeration.ETransactionType;
import com.linkdevcode.banking.payment_service.event.TransactionCompletedEvent;
import com.linkdevcode.banking.payment_service.model.request.DepositRequest;
import com.linkdevcode.banking.payment_service.model.request.DispenseRequest;
import com.linkdevcode.banking.payment_service.model.request.TransferRequest;
import com.linkdevcode.banking.payment_service.outbox.OutboxEventRepository;
import com.linkdevcode.banking.payment_service.model.response.PaymentResponse;
import com.linkdevcode.banking.payment_service.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
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
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    /**
     * Constructor for Dependency Injection.
     */
    public PaymentService(
        TransactionRepository transactionRepository, 
        UserClient userClient,
        OutboxEventRepository outboxEventRepository,
        ObjectMapper objectMapper) {
        this.transactionRepository = transactionRepository;
        this.userClient = userClient;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Processes a deposit request for a user.
     */
    @Transactional
    public PaymentResponse processDeposit(Long userId, DepositRequest request) {
        log.info("Starting deposit to {} for amount {}", userId, request.getAmount());

        // Initialize Transaction record (PENDING)
        Transaction transaction = new Transaction();
        transaction.setSenderId(userId);
        transaction.setRecipientId(userId);
        transaction.setAmount(request.getAmount());
        transaction.setStatus("PENDING");
        transaction.setMessage(request.getMessage());
        transaction.setTransactionType(ETransactionType.DEPOSIT);
        transaction = transactionRepository.save(transaction); // Save to get the transaction ID

        try {
            // Basic Validations
            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                 throw new IllegalArgumentException("Deposit amount must be positive.");
            }

            log.info("Attempting to deposit to user: {}", userId);
            BalanceUpdateRequest depositRequest = new BalanceUpdateRequest();
            depositRequest.setUserId(userId);
            depositRequest.setAmount(request.getAmount());
            userClient.deposit(depositRequest);
            
            // Update Transaction status to SUCCESS
            transaction.setStatus("SUCCESS");
            transactionRepository.save(transaction);
            
            // Create the event payload
            TransactionCompletedEvent event = new TransactionCompletedEvent(
                    transaction.getId(),
                    transaction.getSenderId(),
                    transaction.getRecipientId(),
                    transaction.getAmount(),
                    transaction.getMessage(),
                    transaction.getStatus(),
                    transaction.getTransactionType(),
                    LocalDateTime.now()
                );

            // Create Outbox Event for reliable event publishing
            OutboxEvent outbox = new OutboxEvent();
            outbox.setEventId(UUID.randomUUID().toString());
            outbox.setAggregateType(ETransactionType.DEPOSIT.toString());
            outbox.setAggregateId(transaction.getId().toString());
            outbox.setEventType("DEPOSIT_COMPLETED");
            outbox.setPayload(objectMapper.writeValueAsString(event));
            outbox.setStatus(EOutboxStatus.NEW);
            outbox.setCreatedAt(LocalDateTime.now());
            outboxEventRepository.save(outbox);

            return new PaymentResponse("SUCCESS", transaction.getId(), request.getMessage());

        } catch (HttpClientErrorException e) {
            String reason = "User Service Error: " + e.getResponseBodyAsString();
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                 reason = "Validation/Funds Error: " + e.getResponseBodyAsString();
            }
            log.error("Deposit failed due to User Service error for transaction {}: {}", transaction.getId(), reason);
            
            // Update local status to FAILED
            transaction.setStatus("FAILED");
            transaction.setMessage(reason);
            transactionRepository.save(transaction); 
            
            // Propagate the exception to the controller
            throw new ResponseStatusException(e.getStatusCode(), reason);
            
        } catch (Exception e) {
            log.error("Deposit failed due to unexpected error for transaction {}: {}", transaction.getId(), e.getMessage());
            
            // Update local status to FAILED
            transaction.setStatus("FAILED");
            transaction.setMessage("Unexpected error during deposit: " + e.getMessage());
            transactionRepository.save(transaction);
            
            // Propagate a generic 500 status
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error during deposit.");
        }
    }

    /**
     * Processes a dispense request for a user.
     * @param userId
     * @param request
     * @return
    */
    @Transactional
    public PaymentResponse processDispense(Long userId, DispenseRequest request) {
        log.info("Starting dispense from {} for amount {}", userId, request.getAmount());

        // Initialize Transaction record (PENDING)
        Transaction transaction = new Transaction();
        transaction.setSenderId(userId);
        transaction.setRecipientId(userId);
        transaction.setAmount(request.getAmount());
        transaction.setStatus("PENDING");
        transaction.setMessage(request.getMessage());
        transaction.setTransactionType(ETransactionType.DISPENSE);
        transaction = transactionRepository.save(transaction); // Save to get the transaction ID

        try {
            // Basic Validations
            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                 throw new IllegalArgumentException("Dispense amount must be positive.");
            }

            log.info("Attempting to dispense from user: {}", userId);
            BalanceUpdateRequest dispenseRequest = new BalanceUpdateRequest();
            dispenseRequest.setUserId(userId);
            dispenseRequest.setAmount(request.getAmount());
            userClient.dispense(dispenseRequest);
            
            // Update Transaction status to SUCCESS
            transaction.setStatus("SUCCESS");
            transactionRepository.save(transaction);
            
            // Create the event payload
            TransactionCompletedEvent event = new TransactionCompletedEvent(
                    transaction.getId(),
                    transaction.getSenderId(),
                    transaction.getRecipientId(),
                    transaction.getAmount(),
                    transaction.getMessage(),
                    transaction.getStatus(),
                    transaction.getTransactionType(),
                    LocalDateTime.now()
                );

            // Create Outbox Event for reliable event publishing
            OutboxEvent outbox = new OutboxEvent();
            outbox.setEventId(UUID.randomUUID().toString());
            outbox.setAggregateType(ETransactionType.DISPENSE.toString());
            outbox.setAggregateId(transaction.getId().toString());
            outbox.setEventType("DISPENSE_COMPLETED");
            outbox.setPayload(objectMapper.writeValueAsString(event));
            outbox.setStatus(EOutboxStatus.NEW);
            outbox.setCreatedAt(LocalDateTime.now());
            outboxEventRepository.save(outbox);

            return new PaymentResponse("SUCCESS", transaction.getId(), request.getMessage());

        } catch (HttpClientErrorException e) {
            String reason = "User Service Error: " + e.getResponseBodyAsString();
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                 reason = "Validation/Funds Error: " + e.getResponseBodyAsString();
            }
            log.error("Dispense failed due to User Service error for transaction {}: {}", transaction.getId(), reason);
            
            // Update local status to FAILED
            transaction.setStatus("FAILED");
            transaction.setMessage(reason);
            transactionRepository.save(transaction); 
            
            // Propagate the exception to the controller
            throw new ResponseStatusException(e.getStatusCode(), reason);
            
        } catch (Exception e) {
            log.error("Dispense failed due to unexpected error for transaction {}: {}", transaction.getId(), e.getMessage());
            
            // Update local status to FAILED
            transaction.setStatus("FAILED");
            transaction.setMessage("Unexpected error during dispense: " + e.getMessage());
            transactionRepository.save(transaction);
            
            // Propagate a generic 500 status
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error during dispense.");
        }
    }

    /**
     * Processes a money transfer between two users.
     * This method orchestrates the distributed transaction by calling the User Service
     * to deduct and add balances, and manages local transaction records.
     * @param senderId The ID of the user initiating the transfer (from JWT).
     * @param request The transfer request containing recipient ID, amount, and message.
     * @return TransferResponse indicating success or failure.
     */
    @Transactional
    public PaymentResponse processTransfer(Long senderId, TransferRequest request) {
        log.info("Starting transfer from {} to {} for amount {}", senderId, request.getRecipientId(), request.getAmount());

        // Initialize Transaction record (PENDING)
        Transaction transaction = new Transaction();
        transaction.setSenderId(senderId);
        transaction.setRecipientId(request.getRecipientId());
        transaction.setAmount(request.getAmount());
        transaction.setStatus("PENDING");
        transaction.setMessage(request.getMessage());
        transaction.setTransactionType(ETransactionType.TRANSFER);
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
            BalanceUpdateRequest dispenseRequest = new BalanceUpdateRequest();
            dispenseRequest.setUserId(senderId);
            dispenseRequest.setAmount(request.getAmount());
            userClient.dispense(dispenseRequest);

            // Add money to Recipient
            log.info("Attempting to add balance to recipient: {}", request.getRecipientId());
            BalanceUpdateRequest depositRequest = new BalanceUpdateRequest();
            depositRequest.setUserId(request.getRecipientId());
            depositRequest.setAmount(request.getAmount());
            userClient.deposit(depositRequest);
            
            // --- SUCCESS ---
            
            // Update Transaction status to SUCCESS
            transaction.setStatus("SUCCESS");
            transactionRepository.save(transaction);
            
            // Create the event payload
            TransactionCompletedEvent event = new TransactionCompletedEvent(
                    transaction.getId(),
                    transaction.getSenderId(),
                    transaction.getRecipientId(),
                    transaction.getAmount(),
                    transaction.getMessage(),
                    transaction.getStatus(),
                    transaction.getTransactionType(),
                    LocalDateTime.now()
                );

            // Create Outbox Event for reliable event publishing
            OutboxEvent outbox = new OutboxEvent();
            outbox.setEventId(UUID.randomUUID().toString());
            outbox.setAggregateType(ETransactionType.TRANSFER.toString());
            outbox.setAggregateId(transaction.getId().toString());
            outbox.setEventType("TRANSFER_COMPLETED");
            outbox.setPayload(objectMapper.writeValueAsString(event));
            outbox.setStatus(EOutboxStatus.NEW);
            outbox.setCreatedAt(LocalDateTime.now());
            outboxEventRepository.save(outbox);

            return new PaymentResponse("SUCCESS", transaction.getId(), request.getMessage());

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
}