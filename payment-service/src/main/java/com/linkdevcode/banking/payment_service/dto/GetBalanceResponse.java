package com.linkdevcode.banking.payment_service.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class GetBalanceResponse {

    // User ID for whom the balance is retrieved
    private Long userId;

    // Current balance amount
    private BigDecimal balance;
}
