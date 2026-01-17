package com.linkdevcode.banking.voucher_service.model.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.linkdevcode.banking.voucher_service.enumeration.EVoucherStatus;
import com.linkdevcode.banking.voucher_service.enumeration.EVoucherType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVoucherSearchResponse {
    private String voucherCode;
    private BigDecimal value;
    private EVoucherType voucherType;
    private EVoucherStatus voucherStatus;
    private LocalDateTime issuedAt;
    private LocalDateTime expiredAt;
}