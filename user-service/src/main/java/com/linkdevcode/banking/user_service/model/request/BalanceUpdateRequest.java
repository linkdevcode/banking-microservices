package com.linkdevcode.banking.user_service.model.request;

import java.math.BigDecimal;

import lombok.Data;

/**
 * Request model for updating user balance.
*/
@Data
public class BalanceUpdateRequest {

    // ID of the user whose balance is to be updated.
    private Long userId;

    // Amount to update the balance by (positive or negative).
    private BigDecimal amount;
}
