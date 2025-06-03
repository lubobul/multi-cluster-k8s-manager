package com.multikube_rest_service.services;

import com.multikube_rest_service.auth.JwtUtil;
import com.multikube_rest_service.dtos.auth.JwtResponse;
import com.multikube_rest_service.dtos.auth.LoginRequest;
import com.multikube_rest_service.dtos.auth.RegisterRequest;
import com.multikube_rest_service.dtos.responses.UserDto;
import com.multikube_rest_service.entities.Role;
import com.multikube_rest_service.entities.Tenant;
import com.multikube_rest_service.entities.User;
import com.multikube_rest_service.entities.UserSecret;
import com.multikube_rest_service.mappers.UserMapper;
import com.multikube_rest_service.repositories.RoleRepository;
import com.multikube_rest_service.repositories.TenantRepository;
import com.multikube_rest_service.repositories.UserRepository;
import com.multikube_rest_service.repositories.UserSecretRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link UserAuthService}.
 */
@ExtendWith(MockitoExtension.class)
class UserAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserSecretRepository userSecretRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserMapper userMapper;

    // TenantRepository is not directly used by UserAuthService but Tenant objects are.
    // We'll create Tenant objects manually for tests.

    @InjectMocks
    private UserAuthService userAuthService;

    private Tenant systemTenant;
    private User testUser;
    private UserSecret testUserSecret;
    private Role tenantAdminRole;
    private Role providerAdminRole;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        systemTenant = new Tenant();
        systemTenant.setId(1L);
        systemTenant.setName("System");
        systemTenant.setDescription("System Tenant");
        systemTenant.setIsActive(true);

        tenantAdminRole = new Role();
        tenantAdminRole.setId(1L);
        tenantAdminRole.setName("TENANT_ADMIN");

        providerAdminRole = new Role();
        providerAdminRole.setId(2L);
        providerAdminRole.setName("PROVIDER_ADMIN");

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setTenant(systemTenant);
        testUser.setIsActive(true);
        Set<Role> roles = new HashSet<>();
        roles.add(tenantAdminRole);
        testUser.setRoles(roles);

        testUserSecret = new UserSecret();
        testUserSecret.setId(1L);
        testUserSecret.setUser(testUser);
        testUserSecret.setPassword("hashedPassword");

        // Initialize UserDto for mapper mocking
        UserDto userDto = new UserDto();
        userDto.setId(testUser.getId());
        userDto.setEmail(testUser.getEmail());
        userDto.setUsername(testUser.getUsername());
        // TenantDto and roles mapping would be handled by the actual mapper implementation
        // For unit testing the service, we mock the mapper's output.

        registerRequest = new RegisterRequest(
                "newuser",
                "new@example.com",
                "Password123",
                "TENANT_ADMIN"
        );

        loginRequest = new LoginRequest(
                "test@example.com",
                "password123",
                "System"
        );
    }

    // --- Login Tests ---

    @Test
    void login_success_shouldReturnJwtResponse() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(userSecretRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testUserSecret));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUserSecret.getPassword())).thenReturn(true);
        when(userMapper.toDto(testUser)).thenReturn(new UserDto(testUser.getId(), testUser.getUsername(), testUser.getEmail(), null, null, null)); // Simplified DTO for test
        when(jwtUtil.generateToken(testUser)).thenReturn("test-jwt-token");

        JwtResponse jwtResponse = userAuthService.login(loginRequest);

        assertNotNull(jwtResponse);
        assertEquals("test-jwt-token", jwtResponse.getToken());
        assertNotNull(jwtResponse.getUser());
        assertEquals(testUser.getEmail(), jwtResponse.getUser().getEmail());
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(userSecretRepository).findByUserId(testUser.getId());
        verify(passwordEncoder).matches(loginRequest.getPassword(), testUserSecret.getPassword());
        verify(jwtUtil).generateToken(testUser);
    }

    @Test
    void login_userNotFoundByEmail_shouldThrowIllegalArgumentException() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userAuthService.login(loginRequest);
        });
        assertEquals("The email, password, or organization you entered is incorrect.", exception.getMessage());
    }

    @Test
    void login_tenantMismatch_shouldThrowIllegalArgumentException() {
        testUser.setTenant(new Tenant(2L, "AnotherTenant", "Desc", true, null, null)); // Different tenant
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userAuthService.login(loginRequest);
        });
        assertEquals("The email, password, or organization you entered is incorrect.", exception.getMessage());
    }

    @Test
    void login_userSecretNotFound_shouldThrowIllegalArgumentException() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(userSecretRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userAuthService.login(loginRequest);
        });
        assertEquals("The email, password, or organization you entered is incorrect.", exception.getMessage());
    }

    @Test
    void login_passwordMismatch_shouldThrowIllegalArgumentException() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(userSecretRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testUserSecret));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUserSecret.getPassword())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userAuthService.login(loginRequest);
        });
        assertEquals("The email, password, or organization you entered is incorrect.", exception.getMessage());
    }

    @Test
    void login_emptyEmail_shouldThrowIllegalArgumentException() {
        loginRequest.setEmail("");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userAuthService.login(loginRequest);
        });
        assertEquals("Email, password, and tenant must not be empty", exception.getMessage());
    }

    @Test
    void login_emptyPassword_shouldThrowIllegalArgumentException() {
        loginRequest.setPassword(" "); // Test with whitespace
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userAuthService.login(loginRequest);
        });
        assertEquals("Email, password, and tenant must not be empty", exception.getMessage());
    }

    @Test
    void login_emptyTenant_shouldThrowIllegalArgumentException() {
        loginRequest.setTenant(null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userAuthService.login(loginRequest);
        });
        assertEquals("Email, password, and tenant must not be empty", exception.getMessage());
    }


    // --- registerUserForTenant Tests ---

    @Test
    void registerUserForTenant_success_shouldSaveAndReturnUser() {
        Tenant newTenant = new Tenant(2L, "CustomerA", "A new customer tenant", true, Timestamp.from(Instant.now()), Timestamp.from(Instant.now()));
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(roleRepository.findByName(registerRequest.getRole())).thenReturn(Optional.of(tenantAdminRole));
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPasswordForNewUser");

        // Mock the save operation to return the user with an ID and set secret
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            userToSave.setId(2L); // Simulate ID generation
            // Simulate UserSecret being set and linked
            UserSecret secret = new UserSecret();
            secret.setUser(userToSave);
            secret.setPassword("encodedPasswordForNewUser");
            userToSave.setUserSecret(secret);
            return userToSave;
        });

        User createdUser = userAuthService.registerUserForTenant(registerRequest, newTenant);

        assertNotNull(createdUser);
        assertEquals(registerRequest.getUsername(), createdUser.getUsername());
        assertEquals(registerRequest.getEmail(), createdUser.getEmail());
        assertEquals(newTenant, createdUser.getTenant());
        assertTrue(createdUser.getRoles().contains(tenantAdminRole));
        assertNotNull(createdUser.getUserSecret());
        assertEquals("encodedPasswordForNewUser", createdUser.getUserSecret().getPassword());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUserForTenant_existingEmail_shouldThrowIllegalArgumentException() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);
        Tenant tenant = new Tenant();
        tenant.setId(1L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userAuthService.registerUserForTenant(registerRequest, tenant);
        });
        assertEquals("The email '" + registerRequest.getEmail() + "' already exists.", exception.getMessage());
    }

    @Test
    void registerUserForTenant_existingUsername_shouldThrowIllegalArgumentException() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);
        Tenant tenant = new Tenant();
        tenant.setId(1L);


        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userAuthService.registerUserForTenant(registerRequest, tenant);
        });
        assertEquals("The username '" + registerRequest.getUsername() + "' already exists.", exception.getMessage());
    }

    @Test
    void registerUserForTenant_roleNotFound_shouldThrowIllegalArgumentException() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(roleRepository.findByName(registerRequest.getRole())).thenReturn(Optional.empty());
        Tenant tenant = new Tenant();
        tenant.setId(1L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userAuthService.registerUserForTenant(registerRequest, tenant);
        });
        assertEquals("Role '" + registerRequest.getRole() + "' not found.", exception.getMessage());
    }
    @Test
    void registerUserForTenant_invalidRoleForTenantAdmin_shouldThrowIllegalArgumentException() {
        registerRequest.setRole("PROVIDER_ADMIN"); // An unsuitable role
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(roleRepository.findByName(registerRequest.getRole())).thenReturn(Optional.of(providerAdminRole));
        Tenant tenant = new Tenant();
        tenant.setId(1L);


        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userAuthService.registerUserForTenant(registerRequest, tenant);
        });
        assertEquals("Invalid role '" + registerRequest.getRole() + "' for default tenant admin. Expected 'TENANT_ADMIN'.", exception.getMessage());
    }

    @Test
    void registerUserForTenant_nullTenant_shouldThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userAuthService.registerUserForTenant(registerRequest, null);
        });
        assertEquals("A valid tenant must be provided for user registration.", exception.getMessage());
    }

    @Test
    void registerUserForTenant_nullTenantId_shouldThrowIllegalArgumentException() {
        Tenant tenantWithNullId = new Tenant(); // ID is null
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userAuthService.registerUserForTenant(registerRequest, tenantWithNullId);
        });
        assertEquals("A valid tenant must be provided for user registration.", exception.getMessage());
    }

    // --- Main Register Method Test (Acknowledging its current limitation) ---
    @Test
    void register_whenCalled_shouldThrowUnsupportedOperationException() {
        // Provide valid inputs up to the point of the exception
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(roleRepository.findByName(registerRequest.getRole())).thenReturn(Optional.of(tenantAdminRole));
        // No need to mock passwordEncoder.encode if the exception is thrown before it

        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> {
            userAuthService.register(registerRequest);
        });
        assertEquals("Tenant assignment logic during registration needs to be implemented.", exception.getMessage());
    }

    @Test
    void register_passwordTooShort_shouldThrowIllegalArgumentException() {
        registerRequest.setPassword("12345"); // Less than 6 characters
        // No need for other mocks as this validation happens first in validateRegisterRequest

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userAuthService.register(registerRequest); // Will call private validateRegisterRequest
        });
        assertEquals("Password must be at least 6 characters long", exception.getMessage());
    }

    @Test
    void register_passwordNoLetter_shouldThrowIllegalArgumentException() {
        registerRequest.setPassword("123456"); // No letter
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userAuthService.register(registerRequest);
        });
        assertEquals("Password must contain at least one letter and one number", exception.getMessage());
    }

    @Test
    void register_passwordNoDigit_shouldThrowIllegalArgumentException() {
        registerRequest.setPassword("abcdef"); // No digit
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userAuthService.register(registerRequest);
        });
        assertEquals("Password must contain at least one letter and one number", exception.getMessage());
    }
}