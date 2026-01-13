package com.linkdevcode.banking.history_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.linkdevcode.banking.history_service.enumeration.ETransactionType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCompletedEvent {
    private Long transactionId;
    private Long senderId;
    private Long recipientId;
    private BigDecimal amount;
    private String message;
    private String status;
    private ETransactionType transactionType;
    private LocalDateTime transactionTime;
}
