package com.linkdevcode.banking.user_service.model.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Response model for user details.
 */
@Data
public class UserResponse {

    // Unique identifier of the user.
    private Long id;

    // Username of the user.
    private String username;

    // Email of the user.
    private String email;

    // Full name of the user.
    private String fullName;

    // List of accounts associated with the user.
    private List<AccountInfo> accounts;

    // Roles assigned to the user.
    private Set<String> roles;

    // Status of the user account.
    private String status;
}
