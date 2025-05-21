package com.multikube_rest_service.auth;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private final UserDetails principal;

    public JwtAuthenticationToken(UserDetails principal) {
        super(null); // No authorities passed here
        this.principal = principal;
        setAuthenticated(true); // Mark the token as authenticated
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