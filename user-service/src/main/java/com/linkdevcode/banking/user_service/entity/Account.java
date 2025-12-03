package com.linkdevcode.banking.user_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

import com.linkdevcode.banking.user_service.constant.AppConstants;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    // Use User ID as PK and FK for strict One-to-One mapping
    @Id
    @Column(name = "user_id")
    private Long id; 

    @OneToOne
    @MapsId // Maps the primary key of this entity to the primary key of the User entity
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "currency", nullable = false)
    private String currency = AppConstants.CURRENCY_VND;

    @Column(name = "status", nullable = false)
    private String status = AppConstants.ACCOUNT_STATUS_ACTIVE;
    
}