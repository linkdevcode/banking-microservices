package com.linkdevcode.banking.user_service.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request model for resetting user password.
*/
@Data
public class ResetPasswordRequest {
  
    // New password to be set.
    @NotBlank
    private String newPassword;

    // Token sent to the user's email for password reset verification.
    @NotBlank
    private String resetToken;
}
