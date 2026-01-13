package com.linkdevcode.banking.history_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.linkdevcode.banking.history_service.enumeration.ETransactionType;

@Entity
@Table(
    name = "transaction_history",
    indexes = {
        @Index(name = "idx_sender_id", columnList = "sender_id"),
        @Index(name = "idx_recipient_id", columnList = "recipient_id"),
        @Index(name = "idx_transaction_time", columnList = "transaction_time")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_transaction_id", columnNames = "transaction_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private ETransactionType transactionType;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "message")
    private String message;

    @Column(name = "transaction_time", nullable = false)
    private LocalDateTime transactionTime;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;
}
