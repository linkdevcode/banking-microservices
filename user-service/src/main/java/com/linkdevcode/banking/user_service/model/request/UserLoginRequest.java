package com.linkdevcode.banking.user_service.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request model for user login.
*/
@Data
public class UserLoginRequest {

    // Username of the user trying to log in.
    @NotBlank
    private String username;

    // Password of the user trying to log in.
    @NotBlank
    private String password;
}
