package com.linkdevcode.banking.user_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity to store temporary tokens used for password reset flow.
 */
@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
public class PasswordResetToken {

    @Id
    private Long id;

    // The unique token string sent to the user via email
    @Column(nullable = false, unique = true)
    private String token;

    // The token's expiration timestamp
    @Column(nullable = false)
    private LocalDateTime expiryDate;

    // Foreign Key reference to the User entity
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @MapsId
    @JoinColumn(nullable = false, name = "id", unique = true)
    private User user;
}