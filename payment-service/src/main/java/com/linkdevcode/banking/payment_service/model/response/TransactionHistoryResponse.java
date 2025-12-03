package com.linkdevcode.banking.payment_service.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.linkdevcode.banking.payment_service.entity.Transaction;

/**
 * DTO returned to the client, containing enriched transaction data (including names).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistoryResponse {

    // Unique transaction ID
    private Long transactionId;

    // IDs and Sender names
    private Long senderId;
    private String senderName; // Enriched data

    // IDs and Recipient names
    private Long recipientId;
    private String recipientName; // Enriched data

    // Transaction details
    private BigDecimal amount;
    private String status;
    private String message;
    private LocalDateTime transactionTime;

    // Helper method to create a response from the entity
    public static TransactionHistoryResponse fromEntity(Transaction transaction) {
        TransactionHistoryResponse dto = new TransactionHistoryResponse();
        dto.setTransactionId(transaction.getId());
        dto.setSenderId(transaction.getSenderId());
        dto.setRecipientId(transaction.getRecipientId());
        dto.setAmount(transaction.getAmount());
        dto.setStatus(transaction.getStatus());
        dto.setMessage(transaction.getMessage());
        dto.setTransactionTime(transaction.getTransactionTime());
        // Names will be set later during the enrichment process
        return dto;
    }
}