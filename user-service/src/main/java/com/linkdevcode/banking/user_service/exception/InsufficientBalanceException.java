package com.linkdevcode.banking.user_service.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(BigDecimal balance, BigDecimal amount) {
        super("Insufficient balance. Current balance: " + balance + ", Amount: " + amount);
    }
}
