package com.linkdevcode.banking.user_service.model.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Response model for user details.
 */
@Data
public class UserResponse {

    // Unique identifier of the user.
    private Long userId;

    // Username of the user.
    private String username;

    // Email of the user.
    private String email;

    // Full name of the user.
    private String fullName;

    // Current account balance of the user.
    private BigDecimal accountBalance;

    // Roles assigned to the user.
    private Set<String> roles;

    // Indicates if the user account is enabled.
    private Boolean isEnabled;
}
