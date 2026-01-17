package com.linkdevcode.banking.history_service.model.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.linkdevcode.banking.history_service.enumeration.ETransactionStatus;
import com.linkdevcode.banking.history_service.enumeration.ETransactionType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminHistorySearchResponse {
    private Long transactionId;
    private Long fromUserId;
    private Long toUserId;
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private ETransactionType transactionType;
    private ETransactionStatus status;
    private String message;
    private LocalDateTime transactionTime;
    private LocalDateTime recordedAt;
}