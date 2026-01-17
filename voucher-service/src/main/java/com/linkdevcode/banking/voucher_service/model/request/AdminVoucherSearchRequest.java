package com.linkdevcode.banking.voucher_service.model.request;

import com.linkdevcode.banking.voucher_service.enumeration.EVoucherStatus;
import com.linkdevcode.banking.voucher_service.enumeration.EVoucherType;

import lombok.Data;

@Data
public class AdminVoucherSearchRequest {

    private Long userId;
    private EVoucherType voucherType;
    private EVoucherStatus voucherStatus;

    private int page = 0;
    private int size = 5;
    private String sortBy = "expiredAt";
    private String direction = "DESC";
}
