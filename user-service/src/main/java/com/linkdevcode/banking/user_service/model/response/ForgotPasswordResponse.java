package com.linkdevcode.banking.user_service.model.response;

import lombok.Data;

@Data
public class ForgotPasswordResponse {
    private String resetToken;
    private String message;
}
