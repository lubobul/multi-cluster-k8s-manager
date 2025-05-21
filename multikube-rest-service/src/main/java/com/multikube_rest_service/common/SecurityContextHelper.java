package com.multikube_rest_service.common;

import com.multikube_rest_service.auth.JwtUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityContextHelper {
    public static Long getAuthenticatedUserId() {
        JwtUserDetails userDetails = (JwtUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUserId();
    }
}
