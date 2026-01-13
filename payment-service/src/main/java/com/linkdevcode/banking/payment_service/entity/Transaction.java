package com.linkdevcode.banking.payment_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import com.linkdevcode.banking.payment_service.enumeration.ETransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a money transfer transaction record.
 */
@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user who initiated the transaction (taken from JWT)
    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    // The account ID receiving the money
    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private ETransactionType transactionType;

    // PENDING, SUCCESS, FAILED
    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "message")
    private String message;

    @CreationTimestamp
    @Column(name = "transaction_time", nullable = false, updatable = false)
    private LocalDateTime transactionTime;
}