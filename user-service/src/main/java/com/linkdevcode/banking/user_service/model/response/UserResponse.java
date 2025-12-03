package com.linkdevcode.banking.user_service.model.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class UserResponse {
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private BigDecimal accountBalance;
    private Set<String> roles;
    private Boolean isEnabled;
}
