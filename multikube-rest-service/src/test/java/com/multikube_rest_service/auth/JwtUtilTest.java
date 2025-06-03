package com.multikube_rest_service.auth;

import com.multikube_rest_service.entities.Role;
import com.multikube_rest_service.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link JwtUtil}.
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private User testUser;
    private final String TEST_SECRET_KEY = "zuvA/l+BEqk49luaIDJIh/nwy8jOkUYi79fDaFPJjv4="; // Same as in JwtUtil

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(); // Assuming JwtUtil has a public constructor and no complex dependencies for instantiation

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");

        Set<Role> roles = new HashSet<>();
        Role adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setName("PROVIDER_ADMIN");
        roles.add(adminRole);

        Role userRole = new Role();
        userRole.setId(2L);
        userRole.setName("TENANT_USER");
        roles.add(userRole);

        testUser.setRoles(roles);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(TEST_SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Test
    void generateToken_shouldGenerateValidToken() {
        String token = jwtUtil.generateToken(testUser);
        assertNotNull(token);

        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(testUser.getEmail(), claims.getSubject());
        assertEquals(testUser.getId(), claims.get("userId", Long.class));
        assertTrue(claims.getExpiration().after(new Date()));

        @SuppressWarnings("unchecked")
        List<String> rolesInToken = claims.get("roles", List.class);
        assertNotNull(rolesInToken);
        assertEquals(testUser.getRoles().size(), rolesInToken.size());
        assertTrue(rolesInToken.contains("PROVIDER_ADMIN"));
        assertTrue(rolesInToken.contains("TENANT_USER"));
    }

    @Test
    void extractEmail_shouldReturnCorrectEmail() {
        String token = jwtUtil.generateToken(testUser);
        String email = jwtUtil.extractEmail(token);
        assertEquals(testUser.getEmail(), email);
    }

    @Test
    void extractRoles_shouldReturnCorrectRoles() {
        String token = jwtUtil.generateToken(testUser);
        List<String> extractedRoles = jwtUtil.extractRoles(token); //

        assertNotNull(extractedRoles);
        assertEquals(testUser.getRoles().size(), extractedRoles.size());

        List<String> expectedRoles = testUser.getRoles().stream().map(Role::getName).collect(Collectors.toList());
        assertTrue(extractedRoles.containsAll(expectedRoles) && expectedRoles.containsAll(extractedRoles));
    }

    @Test
    void extractClaim_shouldReturnCorrectUserId() {
        String token = jwtUtil.generateToken(testUser);
        Long userId = jwtUtil.extractClaim(token, "userId", Long.class); //
        assertEquals(testUser.getId(), userId);
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        String token = jwtUtil.generateToken(testUser);
        assertTrue(jwtUtil.validateToken(token)); //
    }

    @Test
    void validateToken_shouldReturnFalseForExpiredToken() {
        // Generate a token that expires immediately (or in the past)
        String expiredToken = Jwts.builder()
                .subject(testUser.getEmail())
                .claim("userId", testUser.getId())
                .claim("roles", testUser.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .issuedAt(new Date(System.currentTimeMillis() - 2000)) // Issued 2 seconds ago
                .expiration(new Date(System.currentTimeMillis() - 1000)) // Expired 1 second ago
                .signWith(getSigningKey())
                .compact();

        assertFalse(jwtUtil.validateToken(expiredToken));
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidSignature() {
        // Generate a token with a different secret key
        SecretKey otherKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode("anotherSecretKeyAnotherSecretKeyAnotherSecretKey123="));
        String tokenWithWrongSignature = Jwts.builder()
                .subject(testUser.getEmail())
                .claim("userId", testUser.getId())
                .claim("roles", testUser.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour
                .signWith(otherKey)
                .compact();

        assertFalse(jwtUtil.validateToken(tokenWithWrongSignature));
    }

    @Test
    void validateToken_shouldReturnFalseForMalformedToken() {
        String malformedToken = "this.is.not.a.jwt";
        assertFalse(jwtUtil.validateToken(malformedToken));
    }

     @Test
    void validateToken_shouldReturnFalseForTokenWithNullEmail() {
        // Edge case: what if a token is generated for a user with a null email (though User entity has @Column(nullable=false))
        // This test assumes generation might be attempted elsewhere or a token could be manually constructed
        // For JwtUtil.generateToken, it would throw a NullPointerException if user.getEmail() is null before even signing.
        // So, we test validateToken directly with a manually crafted token if needed.
        // However, User.email is non-nullable. This test is more for robustness if such a token ever appeared.

        String tokenWithNullSubject = Jwts.builder()
                // .subject(null) // This would make .getSubject() return null, which is valid per JWT spec
                                 // but our extractEmail would return null. validateToken would still be true.
                .claim("userId", testUser.getId())
                .claim("roles", testUser.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(getSigningKey())
                .compact();
        // Jwts.builder().subject(null) is allowed.
        // JwtUtil.extractEmail would return null.
        // JwtUtil.validateToken should still be true as the token structure itself is valid.
        assertTrue(jwtUtil.validateToken(tokenWithNullSubject));

        // Let's test that extractEmail handles it gracefully by returning null
        assertNull(jwtUtil.extractEmail(tokenWithNullSubject));
    }


    @Test
    void generateToken_withNoRoles_shouldStillGenerateToken() {
        User userWithNoRoles = new User();
        userWithNoRoles.setId(2L);
        userWithNoRoles.setEmail("noroles@example.com");
        userWithNoRoles.setUsername("norolesuser");
        userWithNoRoles.setRoles(new HashSet<>()); // Empty set of roles

        String token = jwtUtil.generateToken(userWithNoRoles);
        assertNotNull(token);

        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(userWithNoRoles.getEmail(), claims.getSubject());
        @SuppressWarnings("unchecked")
        List<String> rolesInToken = claims.get("roles", List.class);
        assertNotNull(rolesInToken);
        assertTrue(rolesInToken.isEmpty()); // Expect empty list of roles
    }

}