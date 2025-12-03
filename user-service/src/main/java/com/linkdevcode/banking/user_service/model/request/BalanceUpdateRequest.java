package com.linkdevcode.banking.user_service.model.request;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class BalanceUpdateRequest {
    private BigDecimal amount;
}
