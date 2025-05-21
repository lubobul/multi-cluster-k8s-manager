package com.multikube_rest_service.auth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class JwtUserDetails implements UserDetails {
    private final String email;
    @Getter
    private final Long userId;

    public JwtUserDetails(String email, Long userId) {
        this.email = email;
        this.userId = userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // If you have roles or permissions, you can return them here
        return null; // No roles/authorities in this example
    }

    @Override
    public String getPassword() {
        return null; // Not needed here as JWT already validates the user
    }

    @Override
    public String getUsername() {
        return email; // Email is treated as the username
    }

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
        return true; // Adjust based on your business logic
    }
}
