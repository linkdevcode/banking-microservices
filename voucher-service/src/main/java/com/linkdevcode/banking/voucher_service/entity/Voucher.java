package com.linkdevcode.banking.voucher_service.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.linkdevcode.banking.voucher_service.enumeration.EVoucherStatus;
import com.linkdevcode.banking.voucher_service.enumeration.EVoucherType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vouchers",
       indexes = {
           @Index(name = "idx_voucher_user_type_date",
                  columnList = "user_id, type, issued_at")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Voucher {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private EVoucherType type;

    @Column(name = "value", nullable = false)
    private BigDecimal value;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EVoucherStatus status;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;
}
