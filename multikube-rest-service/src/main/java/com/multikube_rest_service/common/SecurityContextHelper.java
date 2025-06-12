package com.multikube_rest_service.common;

import com.multikube_rest_service.auth.JwtUserDetails;
import com.multikube_rest_service.exceptions.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Helper class for accessing user details from the Spring Security context.
 */
public class SecurityContextHelper {

    /**
     * Retrieves the authenticated user's details.
     *
     * @return The {@link JwtUserDetails} of the currently authenticated user.
     * @throws UnauthorizedException if no user is authenticated.
     */
    public static JwtUserDetails getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof JwtUserDetails)) {
            throw new UnauthorizedException("User is not authenticated.");
        }
        return (JwtUserDetails) authentication.getPrincipal();
    }

    /**
     * Retrieves the ID of the currently authenticated user.
     *
     * @return The user ID.
     */
    public static Long getAuthenticatedUserId() {
        return getAuthenticatedUser().getUserId();
    }

    /**
     * Retrieves the Tenant ID associated with the currently authenticated user.
     *
     * @return The tenant ID.
     * @throws UnauthorizedException if the user is not authenticated.
     * @throws IllegalStateException if the authenticated user is not associated with a tenant.
     */
    public static Long getAuthenticatedTenantId() {
        JwtUserDetails userDetails = getAuthenticatedUser();
        Long tenantId = userDetails.getTenantId();
        if (tenantId == null) {
            // This case might happen for a system-level user not associated with any tenant, which is an invalid state for tenant-specific operations.
            throw new IllegalStateException("Authenticated user does not have an associated tenant ID.");
        }
        return tenantId;
    }
}