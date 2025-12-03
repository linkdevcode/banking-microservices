package com.linkdevcode.banking.user_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import com.linkdevcode.banking.user_service.entity.ERole;
import com.linkdevcode.banking.user_service.entity.Role;
import com.linkdevcode.banking.user_service.entity.User;
import com.linkdevcode.banking.user_service.model.request.UserRegisterRequest;
import com.linkdevcode.banking.user_service.model.response.UserResponse;
import com.linkdevcode.banking.user_service.repository.AccountRepository;
import com.linkdevcode.banking.user_service.repository.RoleRepository;
import com.linkdevcode.banking.user_service.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    // Mock dependencies: We don't want to hit the actual database
    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountRepository accountRepository; // NEW MOCK: For Account Entity operations
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtils jwtUtils;

    // The class we are testing. InjectMocks will create an instance and inject the mocks above.
    @InjectMocks
    private UserService userService;

    private UserRegisterRequest validRegisterRequest;
    private Role userRole;
    private User testUser;
    private Account testAccount;
    private final Long TEST_USER_ID = 1L;
    private final BigDecimal INITIAL_BALANCE = new BigDecimal("100.00");

    @BeforeEach
    void setUp() {
        // Setup a common valid request for reuse
        validRegisterRequest = new UserRegisterRequest();
        validRegisterRequest.setUsername("testuser");
        validRegisterRequest.setEmail("test@example.com");
        validRegisterRequest.setFullName("Test User");
        validRegisterRequest.setPassword("password123");

        // Setup the default role object
        userRole = new Role();
        userRole.setRoleId(1);
        userRole.setName(ERole.ROLE_USER);

        // Setup common entities for internal API tests
        testUser = new User();
        testUser.setUserId(TEST_USER_ID);
        testUser.setUsername("testuser");
        testUser.setIsEnabled(true);
        testUser.setRoles(Set.of(userRole));

        testAccount = new Account();
        testAccount.setId(TEST_USER_ID);
        testAccount.setUser(testUser);
        testAccount.setBalance(INITIAL_BALANCE);
        testAccount.setAccountNumber("1234567890");
        testUser.setAccount(testAccount);
    }

    // --- TEST REGISTER USER (EXTERNAL API) ---

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
        savedUserWithId.setUserId(TEST_USER_ID); 
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

    // --- TEST SEARCH USERS (EXTERNAL API) ---

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

    // --- TEST GET BALANCE (INTERNAL API) ---

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

    // --- TEST DEDUCT BALANCE (INTERNAL API) ---

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

    // --- TEST ADD BALANCE (INTERNAL API) ---

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