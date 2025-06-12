package com.multikube_rest_service.auth;

import com.multikube_rest_service.entities.Role; // Import Role
import com.multikube_rest_service.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List; // Import List
import java.util.stream.Collectors; // Import Collectors

@Component
public class JwtUtil {

    private final String SECRET_KEY = "zuvA/l+BEqk49luaIDJIh/nwy8jOkUYi79fDaFPJjv4="; // Base64-encoded key

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user) {
        // Extract role names from the User entity
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList()); //

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("tenantId", user.getTenant().getId())
                .claim("roles", roles) // Add roles as a claim
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public List<String> extractRoles(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("roles", List.class);
    }


    public <T> T extractClaim(String token, String claimName, Class<T> claimType) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get(claimName, claimType);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            // Log the exception e.g., logger.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
}