package com.multikube_rest_service.controllers;

import com.multikube_rest_service.common.HttpConstants;
import com.multikube_rest_service.dtos.auth.JwtResponse;
import com.multikube_rest_service.dtos.auth.LoginRequest;
import com.multikube_rest_service.rest.RestMessageResponse;
import com.multikube_rest_service.services.UserAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserAuthService userAuthService;

    public AuthController(
            UserAuthService userAuthService
    ) {
        this.userAuthService = userAuthService;
    }

    //TODO Rework to reuse for create user by admin
//    @Operation(
//            summary = "Register a new user",
//            description = "Register a new user in the system by providing user details"
//    )
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "User registered successfully")
//    })
//    @PostMapping("/register")
//    public ResponseEntity<RestMessageResponse> register(@RequestBody RegisterRequest request) {
//        userAuthService.register(request);
//        return ResponseEntity.ok(new RestMessageResponse("User registered successfully"));
//    }

    @Operation(
            summary = "Login and receive JWT token",
            description = "Log in the user and issue a JWT token for authentication"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged in successfully, JWT token returned")
    })
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        JwtResponse jwtToken = userAuthService.login(request); // Obtain the JWT token

        // Set the JWT token as an HTTP-only secure cookie
        ResponseCookie jwtCookie = ResponseCookie.from(HttpConstants.JWT_TOKEN, jwtToken.getToken())
                .httpOnly(true) // Prevent access from JavaScript
                .secure(true) // Use HTTPS
                .sameSite("Strict") // SameSite policy for CSRF protection
                .path("/") // Available for all paths
                .maxAge(10 * 60 * 60) // 10 hours
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
        // Clear the JWT cookie by setting maxAge to 0
        ResponseCookie jwtCookie = ResponseCookie.from(HttpConstants.JWT_TOKEN, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(0) // Clear cookie
                .build();

        response.addHeader("Set-Cookie", jwtCookie.toString());
        return ResponseEntity.ok(new RestMessageResponse("Logged out successfully"));
    }
}
