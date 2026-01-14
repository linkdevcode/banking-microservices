package com.linkdevcode.banking.user_service.model.response;

import com.linkdevcode.banking.user_service.enumeration.EAccountStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountResolveResponse {
    private String accountNumber;
    private Long userId;
    private EAccountStatus status;
}