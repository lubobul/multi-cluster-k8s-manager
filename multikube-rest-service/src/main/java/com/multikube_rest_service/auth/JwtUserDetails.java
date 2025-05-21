package com.multikube_rest_service.auth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections; // Import Collections

public class JwtUserDetails implements UserDetails {
    private final String email;
    @Getter
    private final Long userId;
    private final Collection<? extends GrantedAuthority> authorities; // Added authorities

    public JwtUserDetails(String email, Long userId, Collection<? extends GrantedAuthority> authorities) {
        this.email = email;
        this.userId = userId;
        this.authorities = authorities != null ? authorities : Collections.emptyList(); // Store authorities
    }

    // Constructor without authorities for cases where they might not be immediately available or needed
    // However, for role-based security, you'll primarily use the one with authorities.
    public JwtUserDetails(String email, Long userId) {
        this(email, userId, Collections.emptyList());
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities; // Return stored authorities
    }

    @Override
    public String getPassword() {
        return null; // Not needed here as JWT already validates the user
    }

    @Override
    public String getUsername() {
        return email; // Email is treated as the username
    }

    // Account status methods - adjust based on your 'isActive' field in User entity if needed
    @Override
    public boolean isAccountNonExpired() {
        return true; // Adjust based on your business logic
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Adjust based on your business logic
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Adjust based on your business logic
    }

    @Override
    public boolean isEnabled() {
        return true; // Consider linking this to user.getIsActive() if you load the full User entity
    }
}