package com.multikube_rest_service.services;

import com.multikube_rest_service.auth.JwtUtil;
import com.multikube_rest_service.dtos.auth.JwtResponse;
import com.multikube_rest_service.dtos.auth.LoginRequest;
import com.multikube_rest_service.dtos.auth.RegisterRequest;
import com.multikube_rest_service.entities.Role;
import com.multikube_rest_service.entities.User;
import com.multikube_rest_service.entities.UserSecret;
import com.multikube_rest_service.mappers.UserMapper;
import com.multikube_rest_service.repositories.RoleRepository;
import com.multikube_rest_service.repositories.UserRepository;
import com.multikube_rest_service.repositories.UserSecretRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils; // For StringUtils.hasText

import java.util.HashSet;

@Service
public class UserAuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserSecretRepository userSecretRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    public UserAuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserSecretRepository userSecretRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            UserMapper userMapper
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userSecretRepository = userSecretRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
    }

    @Transactional
    public void register(RegisterRequest request) {
        validateRegisterRequest(request);

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("The email you entered already exists.");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("The username you entered already exists.");
        }

        var userRole = roleRepository.findByName(request.getRole());
        if (userRole.isEmpty()) {
            throw new IllegalArgumentException("The role you entered does not exist.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        // user.setTenant(systemTenant); // EXAMPLE: Assign tenant - THIS NEEDS PROPER LOGIC

        var roles = new HashSet<Role>();
        roles.add(userRole.get());
        user.setRoles(roles);

        UserSecret userSecret = new UserSecret();
        userSecret.setUser(user); // Set the user reference in UserSecret
        userSecret.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setUserSecret(userSecret); // Set the UserSecret reference in User

        // userRepository.save(user); // This will fail if user.tenant is null
        throw new UnsupportedOperationException("Tenant assignment logic during registration needs to be implemented.");
    }

    public JwtResponse login(LoginRequest request) {
        validateLoginRequest(request);

        User userEntity = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("The email, password, or organization you entered is incorrect."));

        // Validate Tenant
        if (userEntity.getTenant() == null ||
                !userEntity.getTenant().getName().equalsIgnoreCase(request.getTenant().trim())) {
            throw new IllegalArgumentException("The email, password, or organization you entered is incorrect.");
        }

        UserSecret userSecret = userSecretRepository.findByUserId(userEntity.getId())
                .orElseThrow(() -> new IllegalArgumentException("The email, password, or organization you entered is incorrect.")); // Should not happen if user exists

        if (passwordEncoder.matches(request.getPassword(), userSecret.getPassword())) {
            return new JwtResponse(userMapper.toDto(userEntity), jwtUtil.generateToken(userEntity));
        }

        throw new IllegalArgumentException("The email, password, or organization you entered is incorrect.");
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (!StringUtils.hasText(request.getUsername()) ||
                !StringUtils.hasText(request.getEmail()) ||
                !StringUtils.hasText(request.getPassword()) ||
                !StringUtils.hasText(request.getRole())) { // Added role check
            throw new IllegalArgumentException("Username, email, password, and role must not be empty");
        }
        // ... (rest of password validation)
        String password = request.getPassword();
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d).+$")) {
            throw new IllegalArgumentException("Password must contain at least one letter and one number");
        }
    }

    private void validateLoginRequest(LoginRequest request) {
        if (!StringUtils.hasText(request.getEmail()) ||
                !StringUtils.hasText(request.getPassword()) ||
                !StringUtils.hasText(request.getTenant())) { // Added tenant check
            throw new IllegalArgumentException("Email, password, and tenant must not be empty");
        }
    }
}