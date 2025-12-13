package com.linkdevcode.banking.payment_service.client.user_service.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO representing minimal user information needed by Payment Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLookupResponse {

    // Fields needed for payment service
    private Long id;

    // Username of the user
    private String username;

    // Full name of the user
    private String fullName;
}
