package com.linkdevcode.banking.payment_service.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.linkdevcode.banking.payment_service.entity.Transaction;
import com.linkdevcode.banking.payment_service.enumeration.ETransactionStatus;

/**
 * DTO returned to the client, containing enriched transaction data (including names).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistoryResponse {

    // Unique transaction ID
    private Long transactionId;

    // Account details
    private String fromAccountNumber;
    private String toAccountNumber;

    // Transaction details
    private BigDecimal amount;
    private ETransactionStatus status;
    private String message;
    private LocalDateTime transactionTime;

    // Helper method to create a response from the entity
    public static TransactionHistoryResponse fromEntity(Transaction transaction) {
        TransactionHistoryResponse dto = new TransactionHistoryResponse();
        dto.setTransactionId(transaction.getId());
        dto.setFromAccountNumber(transaction.getFromAccountNumber());
        dto.setToAccountNumber(transaction.getToAccountNumber());
        dto.setAmount(transaction.getAmount());
        dto.setStatus(transaction.getStatus());
        dto.setMessage(transaction.getMessage());
        dto.setTransactionTime(transaction.getTransactionTime());
        return dto;
    }
}