package com.linkdevcode.banking.user_service.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Data Transfer Object (DTO) used as the response body
 * for successful user authentication (login).
 * * It carries the JWT (JSON Web Token) and essential user information
 * back to the client.
 */
@Data // Generates getters, setters, toString, hashCode, and equals methods
@NoArgsConstructor // Generates a constructor with no arguments
@AllArgsConstructor // Generates a constructor with arguments for all fields
public class JwtResponse {

    /**
     * The actual JSON Web Token (JWT) string generated upon successful login.
     * This token must be sent back by the client in the Authorization header
     * for subsequent secure requests.
     */
    private String token;

    /**
     * Defines the type of token used. Common practice is to use "Bearer".
     */
    private String type = "Bearer";

    /**
     * The username of the authenticated user. Useful for displaying user information
     * on the client side (e.g., "Welcome, [Username]").
     */
    private String username;

    /**
     * A collection of authorities (roles) granted to the authenticated user.
     * This information allows the client to determine which features/UI elements
     * should be visible (e.g., hide admin panel for ROLE_USER).
     */
    private Collection<? extends GrantedAuthority> roles;
}