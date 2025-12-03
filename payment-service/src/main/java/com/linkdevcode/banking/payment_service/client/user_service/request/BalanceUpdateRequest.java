package com.linkdevcode.banking.payment_service.client.user_service.request;

import java.math.BigDecimal;

import lombok.Data;

/**
 * Request DTO for updating user balance.
*/
@Data
public class BalanceUpdateRequest {
    private BigDecimal amount;
}
