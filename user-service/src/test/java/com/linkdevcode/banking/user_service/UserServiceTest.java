package com.linkdevcode.banking.user_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.linkdevcode.banking.user_service.constant.AppConstants;
import com.linkdevcode.banking.user_service.entity.Account;
import com.linkdevcode.banking.user_service.enumeration.ERole;
import com.linkdevcode.banking.user_service.entity.PasswordResetToken;
import com.linkdevcode.banking.user_service.entity.Role;
import com.linkdevcode.banking.user_service.entity.User;
import com.linkdevcode.banking.user_service.exception.InvalidCredentialsException;
import com.linkdevcode.banking.user_service.exception.InvalidTokenException;
import com.linkdevcode.banking.user_service.exception.ResourceNotFoundException;
import com.linkdevcode.banking.user_service.model.request.ChangePasswordRequest;
import com.linkdevcode.banking.user_service.model.request.ResetPasswordRequest;
import com.linkdevcode.banking.user_service.model.request.UserRegisterRequest;
import com.linkdevcode.banking.user_service.model.response.UserResponse;
import com.linkdevcode.banking.user_service.repository.AccountRepository;
import com.linkdevcode.banking.user_service.repository.PasswordResetTokenRepository;
import com.linkdevcode.banking.user_service.repository.RoleRepository;
import com.linkdevcode.banking.user_service.repository.UserRepository;
import com.linkdevcode.banking.user_service.security.JwtTokenProvider;
import com.linkdevcode.banking.user_service.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    // Mock dependencies
    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordResetTokenRepository tokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtUtils;

    // The class we are testing. InjectMocks will create an instance and inject the mocks above.
    @InjectMocks
    private UserService userService;

    private UserRegisterRequest validRegisterRequest;
    private Role userRole;
    private User testUser;
    private Account testAccount;
    private final Long TEST_USER_ID = 1L;
    private final String TEST_USERNAME = "testuser";
    private final String TEST_FULLNAME = "Test User";
    private final String OLD_PASS = "oldPassword";
    private final String ENCODED_OLD_PASS = "encodedOldPassword";
    private final String NEW_PASS = "newPassword";
    private final String ENCODED_NEW_PASS = "encodedNewPassword";
    private final String WRONG_PASS = "wrongPassword";
    private final String TEST_EMAIL = "test@example.com";
    private final BigDecimal INITIAL_BALANCE = new BigDecimal("100.00");

    @BeforeEach
    void setUp() {
        // Setup a common valid request for reuse
        validRegisterRequest = new UserRegisterRequest();
        validRegisterRequest.setUsername(TEST_USERNAME);
        validRegisterRequest.setEmail(TEST_EMAIL);
        validRegisterRequest.setFullName(TEST_FULLNAME);
        validRegisterRequest.setPassword(OLD_PASS);

        // Setup the default role object
        userRole = new Role(1, ERole.ROLE_USER);

        // Setup common entities for internal API tests
        testUser = new User();
        testUser.setId(TEST_USER_ID);
        testUser.setUsername(TEST_USERNAME);
        testUser.setPassword(ENCODED_OLD_PASS);
        testUser.setIsEnabled(true);
        testUser.setRoles(Set.of(userRole));

        testAccount = new Account();
        testAccount.setId(TEST_USER_ID);
        testAccount.setUser(testUser);
        testAccount.setBalance(INITIAL_BALANCE);
        testAccount.setAccountNumber("1234567890");
        testUser.setAccount(testAccount);
    }

    // --- TEST REGISTER USER ---

    /**
     * Test case for successful user registration, ensuring both User and Account entities are created.
     */
    @SuppressWarnings("null")
    @Test
    void registerUser_Success() {
        // Arrange: Define mock behavior
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(any())).thenReturn("hashedPassword");

        User savedUserWithId = new User();
        savedUserWithId.setId(TEST_USER_ID); 
        savedUserWithId.setUsername(validRegisterRequest.getUsername());
        savedUserWithId.setEmail(validRegisterRequest.getEmail());
        savedUserWithId.setRoles(Set.of(userRole));
        // Mock save returning the User with generated ID
        when(userRepository.save(any(User.class))).thenReturn(savedUserWithId); 
        // Mock the Account save operation
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // Act
        UserResponse result = userService.registerUser(validRegisterRequest);

        // Assert: Verify results and interactions
        assertNotNull(result);
        assertEquals(savedUserWithId.getUsername(), result.getUsername());
        assertTrue(result.getRoles().contains(AppConstants.ROLE_USER));
        
        // Verify User save was called once
        verify(userRepository, times(1)).save(any(User.class)); 
        // Verify Account save was called once with the linked entity
        verify(accountRepository, times(1)).save(any(Account.class)); 
    }

    /**
     * Test case to ensure registration fails if the username already exists.
     */
    @SuppressWarnings("null")
    @Test
    void registerUser_ThrowsException_IfUsernameExists() {
        // Arrange
        when(userRepository.existsByUsername(any())).thenReturn(true); 

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.registerUser(validRegisterRequest)
        );
        assertEquals("Error: Username is already taken!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class)); 
        verify(accountRepository, never()).save(any(Account.class));
    }

    /**
     * Test case to ensure registration fails if the default role is not found.
     */
    @SuppressWarnings("null")
    @Test
    void registerUser_ThrowsException_IfRoleNotFound() {
        // Arrange
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.empty()); 

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.registerUser(validRegisterRequest)
        );
        assertEquals("Error: Role is not found. Please initialize roles.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class)); 
    }

    // --- TEST CHANGE PASSWORD ---
    
    /**
     * Test case for successful password change for an authenticated user.
    */
    @Test
    void changePassword_Success() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword(OLD_PASS);
        request.setNewPassword(NEW_PASS);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(OLD_PASS, ENCODED_OLD_PASS)).thenReturn(true);
        when(passwordEncoder.encode(NEW_PASS)).thenReturn(ENCODED_NEW_PASS);

        // Act
        userService.changePassword(TEST_USER_ID, request);

        // Assert
        verify(userRepository, times(1)).save(testUser);
        assertEquals(ENCODED_NEW_PASS, testUser.getPassword());
    }

    @Test
    void changePassword_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword(OLD_PASS);
        request.setNewPassword(NEW_PASS);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            userService.changePassword(TEST_USER_ID, request));
    }

    @Test
    void changePassword_IncorrectOldPassword_ThrowsInvalidCredentialsException() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword(WRONG_PASS);
        request.setNewPassword(NEW_PASS);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(WRONG_PASS, ENCODED_OLD_PASS)).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> 
            userService.changePassword(TEST_USER_ID, request));
        verify(userRepository, never()).save(any(User.class));
    }

    // --- TEST FORGOT PASSWORD ---
    @Test
    void createPasswordResetToken_Success() {
        // Arrange
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        doNothing().when(tokenRepository).deleteByUser_Id(TEST_USER_ID);

        // Act
        userService.createPasswordResetToken(TEST_EMAIL);

        // Assert
        verify(tokenRepository, times(1)).deleteByUser_Id(TEST_USER_ID);
        // Verify that a new token was saved
        verify(tokenRepository, times(1)).save(any(PasswordResetToken.class));
        // verify(emailService, times(1)).sendPasswordResetEmail(anyString(), anyString()); // If email service is implemented
    }

    @Test
    void createPasswordResetToken_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            userService.createPasswordResetToken(TEST_EMAIL));
        verify(tokenRepository, never()).save(any(PasswordResetToken.class));
    }

    // --- TEST RESET PASSWORD ---
    @Test
    void resetPassword_Success() {
        // Arrange
        String tokenValue = "validToken123";
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setResetToken(tokenValue);
        request.setNewPassword(NEW_PASS);
        
        PasswordResetToken validToken = new PasswordResetToken();
        validToken.setToken(tokenValue);
        validToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        validToken.setUser(testUser); // Link token to testUser
        
        when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(validToken));
        when(passwordEncoder.encode(NEW_PASS)).thenReturn("encodedNewPass");

        // Act
        userService.resetPassword(request);

        // Assert
        verify(userRepository, times(1)).save(testUser);
        verify(tokenRepository, times(1)).delete(validToken);
        assertEquals("encodedNewPass", testUser.getPassword());
    }

    @Test
    void resetPassword_InvalidToken_ThrowsInvalidTokenException() {
        // Arrange
        String tokenValue = "invalidToken";
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setResetToken(tokenValue);
        request.setNewPassword(NEW_PASS);
        when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> 
            userService.resetPassword(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetPassword_ExpiredToken_ThrowsInvalidTokenException() {
        // Arrange
        String tokenValue = "expiredToken";
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setResetToken(tokenValue);
        request.setNewPassword(NEW_PASS);
        
        PasswordResetToken expiredToken = new PasswordResetToken();
        expiredToken.setToken(tokenValue);
        // Set expiry date to the past
        expiredToken.setExpiryDate(LocalDateTime.now().minusMinutes(5)); 
        expiredToken.setUser(testUser);

        when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(expiredToken));

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> 
            userService.resetPassword(request));
        
        // Assert that the expired token was deleted
        verify(tokenRepository, times(1)).delete(expiredToken); 
        verify(userRepository, never()).save(any(User.class));
    }

    // --- TEST SEARCH USERS ---

    /**
     * Test case for searching users with no query, returning all users paginated.
     */
    @Test
    void searchUsers_NoQuery_ReturnsAllPaged() {
        // Arrange
        List<User> userList = List.of(testUser);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt"));
        @SuppressWarnings("null")
        Page<User> userPage = new PageImpl<>(userList, pageable, 1);

        when(userRepository.findAll(pageable)).thenReturn(userPage); 

        // Act
        Page<UserResponse> resultPage = userService.searchUsers(null, pageable);

        // Assert
        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        verify(userRepository, times(1)).findAll(pageable); 
    }

    /**
     * Test case for searching users with a specific query, returning filtered results paginated.
     */
    @Test
    void searchUsers_WithQuery_ReturnsFilteredPaged() {
        // Arrange
        String query = "Test";
        List<User> userList = List.of(testUser);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt"));
        @SuppressWarnings("null")
        Page<User> userPage = new PageImpl<>(userList, pageable, 1);

        when(userRepository.findByFullNameContainingIgnoreCase(query, pageable)).thenReturn(userPage); 

        // Act
        Page<UserResponse> resultPage = userService.searchUsers(query, pageable);

        // Assert
        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        verify(userRepository, times(1)).findByFullNameContainingIgnoreCase(query, pageable); 
    }

    // --- TEST GET BALANCE ---

    /**
     * Test case for successfully retrieving the account balance.
     */
    @SuppressWarnings("null")
    @Test
    void getBalance_Success() {
        // Arrange
        when(accountRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testAccount));

        // Act
        BigDecimal balance = userService.getBalance(TEST_USER_ID);

        // Assert
        assertEquals(INITIAL_BALANCE, balance);
        verify(accountRepository, times(1)).findById(TEST_USER_ID);
    }

    /**
     * Test case to ensure getting balance fails if the account is not found.
     */
    @Test
    void getBalance_ThrowsException_AccountNotFound() {
        // Arrange
        when(accountRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> userService.getBalance(anyLong()));
    }

    // --- TEST DEDUCT BALANCE ---

    /**
     * Test case for successfully deducting a valid amount.
     */
    @SuppressWarnings("null")
    @Test
    void deductBalance_Success() {
        // Arrange
        BigDecimal deductionAmount = new BigDecimal("10.00");
        when(accountRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // Act
        userService.deductBalance(TEST_USER_ID, deductionAmount);

        // Assert
        BigDecimal expectedBalance = INITIAL_BALANCE.subtract(deductionAmount);
        assertEquals(expectedBalance, testAccount.getBalance());
        verify(accountRepository, times(1)).save(testAccount);
    }

    /**
     * Test case to ensure deduction fails if the amount is greater than the balance.
     */
    @SuppressWarnings("null")
    @Test
    void deductBalance_ThrowsException_InsufficientFunds() {
        // Arrange
        BigDecimal largeAmount = new BigDecimal("200.00"); // Larger than 100.00
        when(accountRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testAccount));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
                     () -> userService.deductBalance(TEST_USER_ID, largeAmount));
        verify(accountRepository, never()).save(any(Account.class));
    }
    
    /**
     * Test case to ensure deduction fails if the amount is negative or zero.
     */
    @Test
    void deductBalance_ThrowsException_InvalidAmount() {
        // Arrange
        BigDecimal zeroAmount = BigDecimal.ZERO;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
                     () -> userService.deductBalance(TEST_USER_ID, zeroAmount));
        verify(accountRepository, never()).findById(anyLong());
    }

    // --- TEST ADD BALANCE ---

    /**
     * Test case for successfully adding a valid amount.
     */
    @SuppressWarnings("null")
    @Test
    void addBalance_Success() {
        // Arrange
        BigDecimal additionAmount = new BigDecimal("50.00");
        when(accountRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // Act
        userService.addBalance(TEST_USER_ID, additionAmount);

        // Assert
        BigDecimal expectedBalance = INITIAL_BALANCE.add(additionAmount);
        assertEquals(expectedBalance, testAccount.getBalance());
        verify(accountRepository, times(1)).save(testAccount);
    }
    
    /**
     * Test case to ensure addition fails if the amount is negative or zero.
     */
    @Test
    void addBalance_ThrowsException_InvalidAmount() {
        // Arrange
        BigDecimal negativeAmount = new BigDecimal("-10.00");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
                     () -> userService.addBalance(TEST_USER_ID, negativeAmount));
        verify(accountRepository, never()).findById(anyLong());
    }
}