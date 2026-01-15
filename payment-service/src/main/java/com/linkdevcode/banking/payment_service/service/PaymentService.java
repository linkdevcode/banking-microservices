package com.linkdevcode.banking.payment_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import com.linkdevcode.banking.payment_service.client.user_service.UserClient;
import com.linkdevcode.banking.payment_service.client.user_service.request.AccountTransferRequest;
import com.linkdevcode.banking.payment_service.client.user_service.request.BalanceUpdateRequest;
import com.linkdevcode.banking.payment_service.client.user_service.response.AccountResolveResponse;
import com.linkdevcode.banking.payment_service.entity.OutboxEvent;
import com.linkdevcode.banking.payment_service.entity.Transaction;
import com.linkdevcode.banking.payment_service.enumeration.EOutboxStatus;
import com.linkdevcode.banking.payment_service.enumeration.ETransactionStatus;
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
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final UserClient userClient;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    /**
     * Processes a deposit request for a user.
     */
    @Transactional
    public PaymentResponse processDeposit(Long userId, DepositRequest request) {

        // Validate amount
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        // Resolve account
        AccountResolveResponse toAccount =
            userClient.resolve(request.getToAccountNumber());

        // Security check
        if (!toAccount.getUserId().equals(userId)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Account does not belong to current user"
            );
        }

        // Initialize Transaction record (PENDING)
        Transaction transaction = new Transaction();
        transaction.setFromUserId(userId);
        transaction.setToUserId(userId);
        transaction.setFromAccountNumber("");
        transaction.setToAccountNumber(request.getToAccountNumber());
        transaction.setAmount(request.getAmount());
        transaction.setStatus(ETransactionStatus.PENDING);
        transaction.setMessage(request.getMessage());
        transaction.setTransactionType(ETransactionType.DEPOSIT);
        transaction = transactionRepository.save(transaction); // Save to get the transaction ID

        try {            
            // Call User Service to add balance
            userClient.deposit(new BalanceUpdateRequest(
                request.getToAccountNumber(),
                request.getAmount()
            ));
            
            // Update Transaction status to SUCCESS
            transaction.setStatus(ETransactionStatus.SUCCESS);
            transactionRepository.save(transaction);
            
            // Create Outbox Event for reliable event publishing
            publishOutbox(transaction, "DEPOSIT_COMPLETED");

            return new PaymentResponse(ETransactionStatus.SUCCESS, transaction.getId(), request.getMessage());

        } catch (HttpClientErrorException e) {
            String reason = "User Service Error: " + e.getResponseBodyAsString();
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                 reason = "Validation/Funds Error: " + e.getResponseBodyAsString();
            }
            
            // Update local status to FAILED
            transaction.setStatus(ETransactionStatus.FAILED);
            transaction.setMessage(reason);
            transactionRepository.save(transaction); 
            
            // Propagate the exception to the controller
            throw new ResponseStatusException(e.getStatusCode(), reason);
            
        } catch (Exception e) {
            
            // Update local status to FAILED
            transaction.setStatus(ETransactionStatus.FAILED);
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

        // Validate amount
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Dispense amount must be positive");
        }

        // Resolve account
        AccountResolveResponse fromAccount =
            userClient.resolve(request.getFromAccountNumber());

        // Security check
        if (!fromAccount.getUserId().equals(userId)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Account does not belong to current user"
            );
        }

        // Initialize Transaction record (PENDING)
        Transaction transaction = new Transaction();
        transaction.setFromUserId(userId);
        transaction.setToUserId(userId);
        transaction.setFromAccountNumber(request.getFromAccountNumber());
        transaction.setToAccountNumber("");
        transaction.setAmount(request.getAmount());
        transaction.setStatus(ETransactionStatus.PENDING);
        transaction.setMessage(request.getMessage());
        transaction.setTransactionType(ETransactionType.DISPENSE);
        transaction = transactionRepository.save(transaction); // Save to get the transaction ID

        try {
            // Call User Service to deduct balance
            userClient.dispense(new BalanceUpdateRequest(
                request.getFromAccountNumber(),
                request.getAmount()
            ));
            
            // Update Transaction status to SUCCESS
            transaction.setStatus(ETransactionStatus.SUCCESS);
            transactionRepository.save(transaction);
            
            // Create Outbox Event for reliable event publishing
            publishOutbox(transaction, "DISPENSE_COMPLETED");

            return new PaymentResponse(ETransactionStatus.SUCCESS, transaction.getId(), request.getMessage());

        } catch (HttpClientErrorException e) {
            String reason = "User Service Error: " + e.getResponseBodyAsString();
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                 reason = "Validation/Funds Error: " + e.getResponseBodyAsString();
            }
            
            // Update local status to FAILED
            transaction.setStatus(ETransactionStatus.FAILED);
            transaction.setMessage(reason);
            transactionRepository.save(transaction); 
            
            // Propagate the exception to the controller
            throw new ResponseStatusException(e.getStatusCode(), reason);
            
        } catch (Exception e) {
            
            // Update local status to FAILED
            transaction.setStatus(ETransactionStatus.FAILED);
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
    public PaymentResponse processTransfer(Long userId, TransferRequest request){
        
        // Resolve accounts
        AccountResolveResponse fromAccount =
            userClient.resolve(request.getFromAccountNumber());

        AccountResolveResponse toAccount =
            userClient.resolve(request.getToAccountNumber());

        // Security check
        if (!fromAccount.getUserId().equals(userId)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Source account does not belong to current user"
            );
        }

        // Execute tranfer
        return executeTransfer(fromAccount.getUserId(), toAccount.getUserId(), request);
    }

    
    public PaymentResponse systemTransfer(TransferRequest request){
        // Resolve accounts
        AccountResolveResponse fromAccount =
            userClient.resolve(request.getFromAccountNumber());

        AccountResolveResponse toAccount =
            userClient.resolve(request.getToAccountNumber());
        
        // Execute tranfer
        return executeTransfer(fromAccount.getUserId(), toAccount.getUserId(), request);
    }


    @Transactional
    public PaymentResponse executeTransfer(
        Long fromUserId, Long toUserId, TransferRequest request) {

        // Validate amount
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Dispense amount must be positive");
        }

        // Prevent transfer to self
        if (request.getFromAccountNumber().equals(request.getToAccountNumber())) {
            throw new IllegalArgumentException("Cannot transfer to same account");
        }

        // Initialize Transaction record (PENDING)
        Transaction transaction = new Transaction();
        transaction.setFromUserId(fromUserId);
        transaction.setToUserId(toUserId);
        transaction.setFromAccountNumber(request.getFromAccountNumber());
        transaction.setToAccountNumber(request.getToAccountNumber());
        transaction.setAmount(request.getAmount());
        transaction.setStatus(ETransactionStatus.PENDING);
        transaction.setMessage(request.getMessage());
        transaction.setTransactionType(ETransactionType.TRANSFER);
        transaction = transactionRepository.save(transaction);

        try {
            // Call User Service to perform transfer
            userClient.transfer(new AccountTransferRequest(
                request.getFromAccountNumber(),
                request.getToAccountNumber(),
                request.getAmount()
            ));
            
            // Update Transaction status to SUCCESS
            transaction.setStatus(ETransactionStatus.SUCCESS);
            transactionRepository.save(transaction);
            
            // Create Outbox Event for reliable event publishing
            publishOutbox(transaction, "TRANSFER_COMPLETED");

            return new PaymentResponse(ETransactionStatus.SUCCESS, transaction.getId(), request.getMessage());

        } catch (HttpClientErrorException e) {
            String reason = "User Service Error: " + e.getResponseBodyAsString();
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                 reason = "Validation/Funds Error: " + e.getResponseBodyAsString();
            }
            
            // Update local status to FAILED
            transaction.setStatus(ETransactionStatus.FAILED);
            transaction.setMessage(reason);
            transactionRepository.save(transaction); 
            
            // Propagate the exception to the controller
            throw new ResponseStatusException(e.getStatusCode(), reason);
            
        } catch (Exception e) {
            
            // Update local status to FAILED
            transaction.setStatus(ETransactionStatus.FAILED);
            transaction.setMessage("Unexpected error during transfer: " + e.getMessage());
            transactionRepository.save(transaction);
            
            // Propagate a generic 500 status
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error during transfer.");
        }
    }

    // Helper method to create and save an OutboxEvent
    private void publishOutbox(Transaction tx, String eventType) throws Exception {
        TransactionCompletedEvent event = TransactionCompletedEvent.from(tx);

        OutboxEvent outbox = new OutboxEvent();
        outbox.setEventId(UUID.randomUUID().toString());
        outbox.setAggregateType(tx.getTransactionType());
        outbox.setAggregateId(tx.getId().toString());
        outbox.setEventType(eventType);
        outbox.setPayload(objectMapper.writeValueAsString(event));
        outbox.setStatus(EOutboxStatus.NEW);
        outbox.setCreatedAt(LocalDateTime.now());

        outboxEventRepository.save(outbox);
    }
}