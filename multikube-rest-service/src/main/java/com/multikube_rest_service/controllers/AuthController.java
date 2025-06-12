package com.multikube_rest_service.controllers;

import com.multikube_rest_service.common.HttpConstants;
import com.multikube_rest_service.dtos.auth.JwtResponse;
import com.multikube_rest_service.dtos.auth.LoginRequest;
import com.multikube_rest_service.dtos.auth.RegisterRequest; //
import com.multikube_rest_service.rest.RestMessageResponse;
import com.multikube_rest_service.services.UserAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement; // Import for Swagger
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Import PreAuthorize
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserAuthService userAuthService;

    public AuthController(
            UserAuthService userAuthService
    ) {
        this.userAuthService = userAuthService;
    }

    @Operation(
            summary = "Register a new user (Admin Only)", // Updated summary
            description = "Register a new user in the system. This endpoint can only be accessed by users with the 'PROVIDER_ADMIN' role.", // Updated description
            security = @SecurityRequirement(name = "bearerAuth") // If you set up bearerAuth in Swagger OpenAPI config
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have PROVIDER_ADMIN role")
    })
    @PostMapping("/register")
    @PreAuthorize("hasAuthority('PROVIDER_ADMIN')") // Secure this endpoint
    public ResponseEntity<RestMessageResponse> register(@RequestBody RegisterRequest request) { //
        userAuthService.register(request);
        return ResponseEntity.ok(new RestMessageResponse("User registered successfully"));
    }

    @Operation(
            summary = "Login and receive JWT token",
            description = "Log in the user and issue a JWT token for authentication"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged in successfully, JWT token returned")
    })
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        JwtResponse jwtToken = userAuthService.login(request);

        ResponseCookie jwtCookie = ResponseCookie.from(HttpConstants.JWT_TOKEN, jwtToken.getToken())
                .httpOnly(true)
                .secure(true) // Should be true in production with HTTPS
                .sameSite("Strict")
                .path("/")
                .maxAge(10 * 60 * 60)
                .build();

        response.addHeader("Set-Cookie", jwtCookie.toString());
        return ResponseEntity.ok(jwtToken);
    }

    @Operation(
            summary = "Logout user",
            description = "Log out the user by clearing the JWT token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logged out successfully")
    })
    @PostMapping("/logout")
    public ResponseEntity<RestMessageResponse> logout(HttpServletResponse response) {
        ResponseCookie jwtCookie = ResponseCookie.from(HttpConstants.JWT_TOKEN, "")
                .httpOnly(true)
                .secure(true) // Should be true in production with HTTPS
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", jwtCookie.toString());
        return ResponseEntity.ok(new RestMessageResponse("Logged out successfully"));
    }
}