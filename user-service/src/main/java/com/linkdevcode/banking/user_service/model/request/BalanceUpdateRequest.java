package com.linkdevcode.banking.user_service.model.request;

import java.math.BigDecimal;

import lombok.Data;

/**
 * Request model for updating user balance.
*/
@Data
public class BalanceUpdateRequest {

    // Account number associated with the balance update.
    private String accountNumber;

    // Amount to update the balance by (positive or negative).
    private BigDecimal amount;
}
