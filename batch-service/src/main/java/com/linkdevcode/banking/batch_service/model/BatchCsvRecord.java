package com.linkdevcode.banking.batch_service.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchCsvRecord {
    
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private String transferMessage;
}
