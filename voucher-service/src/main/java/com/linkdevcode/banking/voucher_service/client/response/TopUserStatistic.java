package com.linkdevcode.banking.voucher_service.client.response;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TopUserStatistic {
    private Long userId;
    private BigDecimal totalAmount;
}
