package com.linkdevcode.banking.history_service.model.request;

import java.time.LocalDateTime;

import com.linkdevcode.banking.history_service.enumeration.ETransactionStatus;
import com.linkdevcode.banking.history_service.enumeration.ETransactionType;

import lombok.Data;

@Data
public class UserHistorySearchRequest {
    private String accountNumber;
    private ETransactionType transactionType;
    private ETransactionStatus transactionStatus;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;

    private int page = 0;
    private int size = 20;
    private String sortBy = "transactionTime";
    private String direction = "DESC";
}
