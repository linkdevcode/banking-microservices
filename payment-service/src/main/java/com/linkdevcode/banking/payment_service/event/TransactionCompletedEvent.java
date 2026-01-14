package com.linkdevcode.banking.payment_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.linkdevcode.banking.payment_service.entity.Transaction;
import com.linkdevcode.banking.payment_service.enumeration.ETransactionStatus;
import com.linkdevcode.banking.payment_service.enumeration.ETransactionType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCompletedEvent {
    private Long transactionId;
    private Long fromUserId;
    private Long toUserId;
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private String message;
    private ETransactionStatus status;
    private ETransactionType transactionType;
    private LocalDateTime transactionTime;

    public static TransactionCompletedEvent from(Transaction tx) {
        return new TransactionCompletedEvent(
            tx.getId(),
            tx.getFromUserId(),
            tx.getToUserId(),
            tx.getFromAccountNumber(),
            tx.getToAccountNumber(),
            tx.getAmount(),
            tx.getMessage(),
            tx.getStatus(),
            tx.getTransactionType(),
            LocalDateTime.now()
        );
    }
}
