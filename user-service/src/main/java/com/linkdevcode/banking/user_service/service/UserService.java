package com.linkdevcode.banking.user_service.service;

import com.linkdevcode.banking.user_service.entity.Account;
import com.linkdevcode.banking.user_service.entity.ERole;
import com.linkdevcode.banking.user_service.entity.Role;
import com.linkdevcode.banking.user_service.entity.User;
import com.linkdevcode.banking.user_service.model.request.UserLoginRequest;
import com.linkdevcode.banking.user_service.model.request.UserRegisterRequest;
import com.linkdevcode.banking.user_service.model.response.JwtResponse;
import com.linkdevcode.banking.user_service.model.response.UserResponse;
import com.linkdevcode.banking.user_service.repository.AccountRepository;
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
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class responsible for handling core user operations,
 * including registration, authentication, user search/retrieval,
 * and internal account management (balance updates).
 */
@Service
public class UserService {

    // Dependencies required for user management and security operations.
    private final UserRepository userRepository;
    private final AccountRepository accountRepository; // New Dependency
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    /**
     * Constructs the UserService with all necessary repository and security dependencies.
     */
    public UserService(
            UserRepository userRepository,
            AccountRepository accountRepository, // Inject new repository
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            @Lazy AuthenticationManager authenticationManager,
            JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Handles the registration of a new user, initializing the User entity and the associated Account entity.
     */
    @Transactional
    public UserResponse registerUser(UserRegisterRequest request) {
        // Validation to ensure username and email are unique.
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        // Create and populate the User entity.
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        // Encode password before saving for security.
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Assign the default role (ROLE_USER).
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new EntityNotFoundException("Error: Role is not found. Please initialize roles."));

        user.setRoles(new HashSet<>(Set.of(userRole)));

        // Save the User entity first (to get the generated ID).
        User savedUser = userRepository.save(user);

        // Create and link the Account entity.
        Account newAccount = new Account();
        newAccount.setId(savedUser.getUserId()); // Use User ID as PK/FK
        newAccount.setUser(savedUser);
        newAccount.setAccountNumber(UUID.randomUUID().toString().substring(0, 12)); // Simple Account Number generation
        newAccount.setBalance(BigDecimal.ZERO); // Initial balance
        // The save is cascaded, but explicit save ensures creation logic is here.
        accountRepository.save(newAccount); 

        // Map the result to a DTO.
        return mapToUserResponse(savedUser);
    }

    /**
     * Authenticates a user's credentials and generates a JWT upon successful verification.
     */
    public JwtResponse authenticateUser(UserLoginRequest request) {
        // Authenticate using Spring Security's AuthenticationManager.
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        // Set authentication in the context (useful for security chain downstream).
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate and return the JWT token.
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
    @SuppressWarnings("null")
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

    // ----------------------------------------------------------------------
    // INTERNAL API METHODS (Used by other Microservices, e.g., Payment Service)
    // ----------------------------------------------------------------------

    /**
     * Retrieves the current balance for a specific user's account.
     * @param userId The ID of the user.
     * @return The account balance.
     */
    public BigDecimal getBalance(Long userId) {
        @SuppressWarnings("null")
        Account account = accountRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("Error: Account not found for user ID: " + userId));
        
        return account.getBalance(); 
    }

    /**
     * Atomically deducts the specified amount from a user's account balance.
     * Used for transfer/withdrawal operations.
     * @param userId The ID of the user (sender).
     * @param amount The amount to deduct.
     */
    @Transactional
    public void deductBalance(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }
        
        @SuppressWarnings("null")
        Account account = accountRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("Error: Account not found for user ID: " + userId));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds.");
        }

        // Deduct and save
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
    }

    /**
     * Atomically adds the specified amount to a user's account balance.
     * Used for transfer/deposit operations.
     * @param userId The ID of the user (recipient).
     * @param amount The amount to add.
     */
    @Transactional
    public void addBalance(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }
        
        @SuppressWarnings("null")
        Account account = accountRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("Error: Account not found for user ID: " + userId));

        // Add and save
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
    }


    /**
     * Utility method to convert a User entity to a UserResponse DTO.
     * Note: AccountBalance is now retrieved through the Account Entity if needed, 
     * but usually not needed for basic User response.
     */
    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getUserId()); // Use getId() if the field name was changed
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        // response.setAccountBalance(user.getAccount().getBalance()); // Removed this dependency for basic user response
        response.setIsEnabled(user.getIsEnabled());

        // Convert the Set of Role entities to a Set of role names (Strings).
        response.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet()));
        return response;
    }
}