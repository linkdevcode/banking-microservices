package com.linkdevcode.banking.payment_service.client.user_service.response;

import com.linkdevcode.banking.payment_service.enumeration.EAccountStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountResolveResponse {
    private String accountNumber;
    private Long userId;
    private EAccountStatus status;
}