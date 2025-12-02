package com.linkdevcode.banking.user_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
import org.springframework.security.authentication.AuthenticationManager; // Still needed for compilation
import org.springframework.security.crypto.password.PasswordEncoder;

import com.linkdevcode.banking.user_service.dto.UserRegisterRequest;
import com.linkdevcode.banking.user_service.dto.UserResponse;
import com.linkdevcode.banking.user_service.model.ERole;
import com.linkdevcode.banking.user_service.model.Role;
import com.linkdevcode.banking.user_service.model.User;
import com.linkdevcode.banking.user_service.repository.RoleRepository;
import com.linkdevcode.banking.user_service.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    // Mock dependencies: We don't want to hit the actual database
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    // We still need to mock these to satisfy the UserService constructor,
    // even though we won't test them here.
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtils jwtUtils;

    // The class we are testing. InjectMocks will create an instance and inject the mocks above.
    @InjectMocks
    private UserService userService;

    private UserRegisterRequest validRegisterRequest;
    private Role userRole;

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
    }

    // --- TEST REGISTER USER ---

    @Test
    void registerUser_Success() {
        // Arrange: Define mock behavior
        when(userRepository.existsByUsername(any())).thenReturn(false); // Username does not exist
        when(userRepository.existsByEmail(any())).thenReturn(false);   // Email does not exist
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(any())).thenReturn("hashedPassword"); // Simulate password hashing

        // Create the expected User entity after saving
        User savedUser = new User();
        savedUser.setUserId(1L);
        savedUser.setUsername(validRegisterRequest.getUsername());
        savedUser.setEmail(validRegisterRequest.getEmail());
        savedUser.setFullName(validRegisterRequest.getFullName());
        savedUser.setPassword("hashedPassword");
        savedUser.setAccountBalance(BigDecimal.ZERO);
        savedUser.setIsEnabled(true);
        savedUser.setRoles(Set.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(savedUser); // Simulate successful save

        // Act
        UserResponse result = userService.registerUser(validRegisterRequest);

        // Assert: Verify results and interactions
        assertNotNull(result);
        assertEquals(savedUser.getUsername(), result.getUsername());
        assertTrue(result.getRoles().contains("ROLE_USER"));
        verify(userRepository, times(1)).save(any(User.class)); // Ensure save was called once
    }

    @Test
    void registerUser_ThrowsException_IfUsernameExists() {
        // Arrange
        when(userRepository.existsByUsername(any())).thenReturn(true); // Simulate username collision

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.registerUser(validRegisterRequest)
        );
        assertEquals("Error: Username is already taken!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class)); // Ensure save was NOT called
    }

    @Test
    void registerUser_ThrowsException_IfRoleNotFound() {
        // Arrange
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.empty()); // Simulate missing Role

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.registerUser(validRegisterRequest)
        );
        assertEquals("Error: Role is not found. Please initialize roles.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class)); // Ensure save was NOT called
    }

    // --- TEST SEARCH USERS ---

    @Test
    void searchUsers_NoQuery_ReturnsAllPaged() {
        // Arrange: Setup data for pagination test
        User user1 = new User();
        user1.setUserId(2L);
        user1.setUsername("user2");
        user1.setEmail("a@a.com");
        user1.setFullName("Alice");
        user1.setPassword("hash");
        user1.setAccountBalance(BigDecimal.ONE);
        user1.setIsEnabled(true);
        user1.setRoles(Set.of(userRole));

        User user2 = new User();
        user2.setUserId(3L);
        user2.setUsername("user3");
        user2.setEmail("b@b.com");
        user2.setFullName("Bob");
        user2.setPassword("hash");
        user2.setAccountBalance(BigDecimal.ONE);
        user2.setIsEnabled(true);
        user2.setRoles(Set.of(userRole));

        List<User> userList = List.of(user1, user2);

        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt"));
        Page<User> userPage = new PageImpl<>(userList, pageable, 2);

        when(userRepository.findAll(pageable)).thenReturn(userPage); // Mock findAll for null query

        // Act
        Page<UserResponse> resultPage = userService.searchUsers(null, pageable);

        // Assert
        assertNotNull(resultPage);
        assertEquals(2, resultPage.getTotalElements());
        assertEquals("Alice", resultPage.getContent().get(0).getFullName());
        verify(userRepository, times(1)).findAll(pageable); // Verify findAll was called
        verify(userRepository, never()).findByFullNameContainingIgnoreCase(any(), any()); // Verify the other method was not called
    }

    @Test
    void searchUsers_WithQuery_ReturnsFilteredPaged() {
        // Arrange: Setup data for filtering test
        String query = "Alice";
        User user = new User();
        user.setUserId(2L);
        user.setUsername("user2");
        user.setEmail("a@a.com");
        user.setFullName("Alice Smith");
        user.setPassword("hash");
        user.setAccountBalance(BigDecimal.ONE);
        user.setIsEnabled(true);
        user.setRoles(Set.of(userRole));
        List<User> userList = List.of(user);

        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt"));
        Page<User> userPage = new PageImpl<>(userList, pageable, 1);

        when(userRepository.findByFullNameContainingIgnoreCase(query, pageable)).thenReturn(userPage); // Mock filtering method

        // Act
        Page<UserResponse> resultPage = userService.searchUsers(query, pageable);

        // Assert
        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        assertEquals("Alice Smith", resultPage.getContent().get(0).getFullName());
        verify(userRepository, times(1)).findByFullNameContainingIgnoreCase(query, pageable); // Verify filtering method was called
    }

    public UserServiceTest() {
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public RoleRepository getRoleRepository() {
        return roleRepository;
    }

    public void setRoleRepository(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public JwtUtils getJwtUtils() {
        return jwtUtils;
    }

    public void setJwtUtils(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public UserRegisterRequest getValidRegisterRequest() {
        return validRegisterRequest;
    }

    public void setValidRegisterRequest(UserRegisterRequest validRegisterRequest) {
        this.validRegisterRequest = validRegisterRequest;
    }

    public Role getUserRole() {
        return userRole;
    }

    public void setUserRole(Role userRole) {
        this.userRole = userRole;
    }

    @Override
    public String toString() {
        return "UserServiceTest [userRepository=" + userRepository + ", roleRepository=" + roleRepository
                + ", passwordEncoder=" + passwordEncoder + ", authenticationManager=" + authenticationManager
                + ", jwtUtils=" + jwtUtils + ", userService=" + userService + ", validRegisterRequest="
                + validRegisterRequest + ", userRole=" + userRole + ", getAuthenticationManager()="
                + getAuthenticationManager() + ", getJwtUtils()=" + getJwtUtils() + ", getPasswordEncoder()="
                + getPasswordEncoder() + ", getRoleRepository()=" + getRoleRepository() + ", getUserRepository()="
                + getUserRepository() + ", getUserRole()=" + getUserRole() + ", getUserService()=" + getUserService()
                + ", getValidRegisterRequest()=" + getValidRegisterRequest() + ", hashCode()=" + hashCode()
                + ", getClass()=" + getClass() + ", toString()=" + super.toString() + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UserServiceTest other = (UserServiceTest) obj;
        if (userRepository == null) {
            if (other.userRepository != null)
                return false;
        } else if (!userRepository.equals(other.userRepository))
            return false;
        if (roleRepository == null) {
            if (other.roleRepository != null)
                return false;
        } else if (!roleRepository.equals(other.roleRepository))
            return false;
        if (passwordEncoder == null) {
            if (other.passwordEncoder != null)
                return false;
        } else if (!passwordEncoder.equals(other.passwordEncoder))
            return false;
        if (authenticationManager == null) {
            if (other.authenticationManager != null)
                return false;
        } else if (!authenticationManager.equals(other.authenticationManager))
            return false;
        if (jwtUtils == null) {
            if (other.jwtUtils != null)
                return false;
        } else if (!jwtUtils.equals(other.jwtUtils))
            return false;
        if (userService == null) {
            if (other.userService != null)
                return false;
        } else if (!userService.equals(other.userService))
            return false;
        if (validRegisterRequest == null) {
            if (other.validRegisterRequest != null)
                return false;
        } else if (!validRegisterRequest.equals(other.validRegisterRequest))
            return false;
        if (userRole == null) {
            if (other.userRole != null)
                return false;
        } else if (!userRole.equals(other.userRole))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((userRepository == null) ? 0 : userRepository.hashCode());
        result = prime * result + ((roleRepository == null) ? 0 : roleRepository.hashCode());
        result = prime * result + ((passwordEncoder == null) ? 0 : passwordEncoder.hashCode());
        result = prime * result + ((authenticationManager == null) ? 0 : authenticationManager.hashCode());
        result = prime * result + ((jwtUtils == null) ? 0 : jwtUtils.hashCode());
        result = prime * result + ((userService == null) ? 0 : userService.hashCode());
        result = prime * result + ((validRegisterRequest == null) ? 0 : validRegisterRequest.hashCode());
        result = prime * result + ((userRole == null) ? 0 : userRole.hashCode());
        return result;
    }
}