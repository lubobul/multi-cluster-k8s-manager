package com.multikube_rest_service.auth;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority; // Import GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection; // Import Collection

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private final UserDetails principal;

    // Constructor accepting UserDetails (which now contains authorities)
    public JwtAuthenticationToken(UserDetails principal) {
        super(principal.getAuthorities()); // Pass authorities to super class
        this.principal = principal;
        setAuthenticated(true); // Mark the token as authenticated
    }

    // Alternative constructor if you want to pass authorities explicitly
    // (though getting them from UserDetails is standard)
    public JwtAuthenticationToken(UserDetails principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        setAuthenticated(true);
    }


    @Override
    public Object getCredentials() {
        return null; // No credentials needed as JWT handles authentication
    }

    @Override
    public Object getPrincipal() {
        return principal; // The authenticated user
    }
}