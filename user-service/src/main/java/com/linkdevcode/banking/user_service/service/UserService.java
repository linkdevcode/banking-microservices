package com.linkdevcode.banking.user_service.service;

import com.linkdevcode.banking.user_service.dto.*;
import com.linkdevcode.banking.user_service.model.ERole;
import com.linkdevcode.banking.user_service.model.Role;
import com.linkdevcode.banking.user_service.model.User;
import com.linkdevcode.banking.user_service.repository.RoleRepository;
import com.linkdevcode.banking.user_service.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class responsible for handling core user operations,
 * including registration, authentication, and user search/retrieval.
 */
@Service
public class UserService {

    // Dependencies required for user management and security operations.
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    /**
     * Constructs the UserService with all necessary repository and security dependencies.
     */
    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            @Lazy AuthenticationManager authenticationManager,
            JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Handles the registration of a new user, including uniqueness validation and role assignment.
     */
    @Transactional
    public UserResponse registerUser(UserRegisterRequest request) {
        // 1. Validation to ensure username and email are unique.
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        // 2. Create and populate the User entity.
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        // Encode password before saving for security.
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // Initialize the user's account balance to zero.
        user.setAccountBalance(BigDecimal.ZERO);

        // 3. Assign the default role (ROLE_USER).
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new EntityNotFoundException("Error: Role is not found. Please initialize roles."));

        user.setRoles(new HashSet<>(Set.of(userRole)));

        // 4. Save the user to the database and map the result to a DTO.
        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    /**
     * Authenticates a user's credentials and generates a JWT upon successful verification.
     */
    public JwtResponse authenticateUser(UserLoginRequest request) {
        // 1. Authenticate using Spring Security's AuthenticationManager.
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        // 2. Set authentication in the context (can be useful but optional in stateless JWT context).
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Generate and return the JWT token.
        String jwt = jwtUtils.generateJwtToken(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        return new JwtResponse(
            jwt,
            "Bearer",
            userDetails.getUsername(),
            userDetails.getAuthorities()
        );
    }

    /**
     * Retrieves a paginated list of users, with optional filtering by full name.
     */
    public Page<UserResponse> searchUsers(String query, Pageable pageable) {
        Page<User> userPage;

        // Check if a search query is provided.
        if (query == null || query.trim().isEmpty()) {
            // Find all users with the provided pagination criteria.
            userPage = userRepository.findAll(pageable);
        } else {
            // Find users whose full name contains the query (case-insensitive).
            userPage = userRepository.findByFullNameContainingIgnoreCase(query, pageable);
        }

        // Map the paginated User entities to a paginated UserResponse DTO.
        return userPage.map(this::mapToUserResponse);
    }

    /**
     * Utility method to convert a User entity to a UserResponse DTO.
     */
    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setAccountBalance(user.getAccountBalance());
        response.setIsEnabled(user.getIsEnabled());

        // Convert the Set of Role entities to a Set of role names (Strings).
        response.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet()));
        return response;
    }
}