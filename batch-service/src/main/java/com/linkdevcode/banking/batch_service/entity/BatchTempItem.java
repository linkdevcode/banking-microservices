package com.linkdevcode.banking.batch_service.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "batch_temp_items")
@Data
public class BatchTempItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_temp_id", nullable = false)
    private String batchTempId;

    @Column(name = "from_account_number", nullable = false)
    private String fromAccountNumber;

    @Column(name = "to_account_number", nullable = false)
    private String toAccountNumber;
    
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "message")
    private String message;

    @Column(name = "valid", nullable = false)
    private boolean valid;

    @Column(name = "error_reason")
    private String errorReason;
}
