package com.linkdevcode.banking.user_service.service;

import com.linkdevcode.banking.user_service.entity.*;
import com.linkdevcode.banking.user_service.enumeration.EAccountStatus;
import com.linkdevcode.banking.user_service.enumeration.ERole;
import com.linkdevcode.banking.user_service.enumeration.EUserStatus;
import com.linkdevcode.banking.user_service.exception.*;
import com.linkdevcode.banking.user_service.model.request.*;
import com.linkdevcode.banking.user_service.model.response.JwtResponse;
import com.linkdevcode.banking.user_service.model.response.UserResponse;
import com.linkdevcode.banking.user_service.repository.*;
import com.linkdevcode.banking.user_service.repository.specification.UserSpecification;
import com.linkdevcode.banking.user_service.security.JwtTokenProvider;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core service handling user lifecycle, authentication,
 * password security flows, and internal account operations.
 */
@Service
@Slf4j
public class UserService {

    // =========================
    // Dependencies
    // =========================
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(
            UserRepository userRepository,
            AccountRepository accountRepository,
            RoleRepository roleRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // =================================================================
    // Registration & Authentication
    // =================================================================

    /**
     * Registers a new user and initializes the associated account.
     */
    @Transactional
    public UserResponse registerUser(UserRegisterRequest request) {

        // Validate uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use.");
        }

        // Create User
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setFullName(request.getFullName());
        user.setStatus(EUserStatus.ACTIVE);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPasswordChangedAt(Instant.now()); // initial security marker

        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new EntityNotFoundException("ROLE_USER not initialized"));

        user.setRoles(Set.of(userRole));

        // Create Account
        Account account = new Account();
        account.setAccountNumber(UUID.randomUUID().toString().substring(0, 12));
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(EAccountStatus.ACTIVE);

        // Link User and Account
        account.setUser(user);
        user.setAccount(account);

        // Save User (cascades to Account)
        User savedUser = userRepository.save(user);

        // Return response
        return mapToUserResponse(savedUser);
    }

    /**
     * Authenticates user and generates JWT token upon successful login.
    */
    public JwtResponse authenticateUser(UserLoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        List<String> roles = user.getRoles()
                .stream()
                .map(r -> r.getName().name())
                .toList();

        String token = jwtTokenProvider.generateToken(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles
        );

        return new JwtResponse(
                token,
                "Bearer",
                user.getUsername(),
                roles
        );
    }

    // =================================================================
    // User Query & Search
    // =================================================================

    public UserResponse getUserProfile(Long userId) {
        if (userId == null) {
            throw new IllegalStateException("UserId is missing");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found: " + userId));

        return mapToUserResponse(user);
    }

    public Page<UserResponse> searchUsers(UserSearchRequest request) {

        Sort sort = Sort.by(
            Sort.Direction.fromString(
                request.direction() != null ? request.direction() : "ASC"
            ),
            request.sortBy() != null ? request.sortBy() : "createdAt"
        );
        
        Pageable pageable = PageRequest.of(
            request.page(),
            request.size(),
            sort
        );

        Specification<User> specification = UserSpecification.search(request);

        return userRepository.findAll(specification, pageable)
             .map(this::mapToUserResponse);
    }

    // =================================================================
    // Password Management (Authenticated)
    // =================================================================

    /**
     * Changes password for authenticated user.
     * All existing tokens become invalid after this operation.
     */
    public void changePassword(Long userId, ChangePasswordRequest request) {

        // Validate userId presence
        if (userId == null) {
            throw new IllegalStateException("UserId is missing");
        }

        // Validate current password
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found: " + userId));
        if (!passwordEncoder.matches(
                request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(Instant.now());

        userRepository.save(user);
    }

    // =================================================================
    // Password Reset (Forgot Password Flow)
    // =================================================================

    /**
     * Generates a reset token and sends email (email sending mocked).
     */
    public String createPasswordResetToken(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with email: " + email));

        tokenRepository.deleteByUser_Id(user.getId());

        PasswordResetToken token = new PasswordResetToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusHours(1));

        tokenRepository.save(token);

        log.info("Password reset token generated for {}: {}", email, token.getToken());
        
        return token.getToken();
    }

    /**
     * Resets password using a valid reset token.
     * Forces logout of all existing sessions.
     */
    public void resetPassword(ResetPasswordRequest request) {

        PasswordResetToken resetToken = tokenRepository.findByToken(request.getResetToken())
                .orElseThrow(() ->
                        new InvalidTokenException("Invalid or expired reset token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new InvalidTokenException("Invalid or expired reset token");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(Instant.now());

        userRepository.save(user);
        tokenRepository.delete(resetToken);
    }

    // =================================================================
    // Account Operations
    // =================================================================

    // Get current balance
    public BigDecimal getBalance(Long userId) {

        Account account = accountRepository.findById(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Account not found: " + userId));

        return account.getBalance();
    }

    // Add balance (deposit)
    @Transactional
    public void deposit(Long userId, BigDecimal amount) {

        validateAmount(amount);

        Account account = accountRepository.findById(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Account not found: " + userId));

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
    }

    // Deduct balance (dispense)
    @Transactional
    public void dispense(Long userId, BigDecimal amount) {

        validateAmount(amount);

        Account account = accountRepository.findById(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Account not found: " + userId));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(account.getBalance(), amount);
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
    }

    // =================================================================
    // Helpers
    // =================================================================

    private void validateAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }
    }

    private UserResponse mapToUserResponse(User user) {

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setStatus(user.getStatus().name());
        response.setAccountBalance(user.getAccount().getBalance());

        response.setRoles(
                user.getRoles()
                        .stream()
                        .map(r -> r.getName().name())
                        .collect(Collectors.toSet())
        );

        response.setAccountBalance(
            user.getAccount() != null 
                ? user.getAccount().getBalance() 
                : BigDecimal.ZERO
        );

        return response;
    }
}