package com.linkdevcode.banking.payment_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private LocalDateTime timestamp;
}
