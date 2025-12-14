package com.linkdevcode.banking.user_service.service;

import com.linkdevcode.banking.user_service.entity.*;
import com.linkdevcode.banking.user_service.enumeration.ERole;
import com.linkdevcode.banking.user_service.exception.*;
import com.linkdevcode.banking.user_service.model.request.*;
import com.linkdevcode.banking.user_service.model.response.JwtResponse;
import com.linkdevcode.banking.user_service.model.response.UserResponse;
import com.linkdevcode.banking.user_service.repository.*;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

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
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(
            UserRepository userRepository,
            AccountRepository accountRepository,
            RoleRepository roleRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            @Lazy AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
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

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPasswordChangedAt(Instant.now()); // initial security marker

        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new EntityNotFoundException("ROLE_USER not initialized"));

        user.setRoles(Set.of(userRole));

        User savedUser = userRepository.save(user);

        Account account = new Account();
        account.setId(savedUser.getId());
        account.setUser(savedUser);
        account.setAccountNumber(UUID.randomUUID().toString().substring(0, 12));
        account.setBalance(BigDecimal.ZERO);

        accountRepository.save(account);

        return mapToUserResponse(savedUser);
    }

    /**
     * Authenticates credentials and issues JWT.
     */
    public JwtResponse authenticateUser(UserLoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtTokenProvider.generateJwtToken(authentication);
        UserDetails principal = (UserDetails) authentication.getPrincipal();

        return new JwtResponse(
                jwt,
                "Bearer",
                principal.getUsername(),
                principal.getAuthorities()
        );
    }

    // =================================================================
    // User Query & Search
    // =================================================================

    public UserResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found: " + userId));

        return mapToUserResponse(user);
    }

    public Page<UserResponse> searchUsers(String query, Pageable pageable) {

        Page<User> page = (query == null || query.isBlank())
                ? userRepository.findAll(pageable)
                : userRepository.findByFullNameContainingIgnoreCase(query, pageable);

        return page.map(this::mapToUserResponse);
    }

    // =================================================================
    // Password Management (Authenticated)
    // =================================================================

    /**
     * Changes password for authenticated user.
     * All existing tokens become invalid after this operation.
     */
    public void changePassword(Long userId, ChangePasswordRequest request) {

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
    public void createPasswordResetToken(String email) {

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

    public BigDecimal getBalance(Long userId) {

        Account account = accountRepository.findById(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Account not found: " + userId));

        return account.getBalance();
    }

    @Transactional
    public void deductBalance(Long userId, BigDecimal amount) {

        validateAmount(amount);

        Account account = accountRepository.findById(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Account not found: " + userId));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds.");
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
    }

    @Transactional
    public void addBalance(Long userId, BigDecimal amount) {

        validateAmount(amount);

        Account account = accountRepository.findById(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Account not found: " + userId));

        account.setBalance(account.getBalance().add(amount));
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
        response.setIsEnabled(user.getIsEnabled());
        response.setAccountBalance(user.getAccount().getBalance());

        response.setRoles(
                user.getRoles()
                        .stream()
                        .map(r -> r.getName().name())
                        .collect(Collectors.toSet())
        );

        return response;
    }
}