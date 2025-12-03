package com.linkdevcode.banking.user_service.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
