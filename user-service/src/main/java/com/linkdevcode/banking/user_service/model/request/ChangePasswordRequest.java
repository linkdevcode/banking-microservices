package com.linkdevcode.banking.user_service.model.request;

import lombok.Data;

/**
 * Request model for changing user password.
*/
@Data
public class ChangePasswordRequest {
  
    // Current password of the user.
    private String currentPassword;

    // New password to be set.
    private String newPassword;
}
