package com.linkdevcode.banking.user_service.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request model for changing user password.
*/
@Data
public class ChangePasswordRequest {
  
    // Current password of the user.
    @NotBlank
    private String currentPassword;

    // New password to be set.
    @NotBlank
    private String newPassword;
}
