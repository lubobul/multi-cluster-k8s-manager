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
import java.util.HashSet;
import java.util.Optional;

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

        if(userRole.isEmpty()) {
            throw new IllegalArgumentException("The role you entered does not exist.");
        }

        // Create and save the User entity with the avatar
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        var roles = new HashSet<Role>();
        roles.add(userRole.get());
        user.setRoles(roles);

        // Create and save the UserSecret entity with the password
        UserSecret userSecret = new UserSecret();
        userSecret.setUser(user);
        userSecret.setPassword(passwordEncoder.encode(request.getPassword())); // Hash the password

        user.setUserSecret(userSecret);

        userRepository.save(user);
    }

    public JwtResponse login(LoginRequest request) {
        validateLoginRequest(request);

        Optional<User> user = userRepository.findByEmail(request.getEmail());

        if (user.isEmpty()) {
            throw new IllegalArgumentException("The email or password you entered is incorrect.");
        }

        Optional<UserSecret> userSecret = userSecretRepository.findByUserId(user.get().getId());

        if (userSecret.isEmpty()) {
            throw new IllegalArgumentException("The email or password you entered is incorrect.");
        }

        if (passwordEncoder.matches(request.getPassword(), userSecret.get().getPassword())) {
            return new JwtResponse(userMapper.toDto(user.get()), jwtUtil.generateToken(user.get()));
        }

        throw new IllegalArgumentException("The email or password you entered is incorrect.");
    }

    public Optional<UserSecret> findUserSecretByUserId(Long userId) {
        return userSecretRepository.findByUserId(userId);
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (isNullOrEmpty(request.getUsername()) ||
                isNullOrEmpty(request.getEmail()) ||
                isNullOrEmpty(request.getPassword())) {
            throw new IllegalArgumentException("Username, email, and password must not be empty");
        }

        String password = request.getPassword();
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }

        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d).+$")) {
            throw new IllegalArgumentException("Password must contain at least one letter and one number");
        }
    }

    private void validateLoginRequest(LoginRequest request) {
        if (isNullOrEmpty(request.getEmail()) || isNullOrEmpty(request.getPassword())) {
            throw new IllegalArgumentException("Email and password must not be empty");
        }
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
