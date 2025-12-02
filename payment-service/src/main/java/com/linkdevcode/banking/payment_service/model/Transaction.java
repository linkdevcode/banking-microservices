package com.linkdevcode.banking.payment_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key to link to the User in User Service (No actual DB constraint)
    @Column(name = "user_id", nullable = false)
    private Long userId; 

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId; // ID of the receiving user

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType; // e.g., DEPOSIT, WITHDRAWAL, TRANSFER

    @Column(name = "status", nullable = false)
    private String status; // e.g., PENDING, SUCCESS, FAILED

    @Column(name = "transaction_time", nullable = false)
    private LocalDateTime transactionTime;
}
