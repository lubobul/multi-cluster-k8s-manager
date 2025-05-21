package com.multikube_rest_service.filters;

import com.multikube_rest_service.auth.JwtAuthenticationToken;
import com.multikube_rest_service.auth.JwtUserDetails;
import com.multikube_rest_service.auth.JwtUtil;
import com.multikube_rest_service.common.HttpConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String cookieHeader = request.getHeader("Cookie");

        if (cookieHeader != null) {
            // Split cookies into individual key-value pairs
            String[] cookies = cookieHeader.split(";");

            for (String cookie : cookies) {
                String[] cookiePair = cookie.trim().split("=", 2);
                String cookieName = cookiePair[0];
                String cookieValue = cookiePair.length > 1 ? cookiePair[1] : "";

                // Check if the JWT_TOKEN cookie exists
                if (HttpConstants.JWT_TOKEN.equals(cookieName)) {
                    String jwtToken = cookieValue;

                    // Validate and process the token
                    if (jwtUtil.validateToken(jwtToken)) {
                        String email = jwtUtil.extractEmail(jwtToken);
                        Long userId = jwtUtil.extractClaim(jwtToken, "userId", Long.class); // Extract userId


                        // Create an Authentication object and set it in the SecurityContext
                        JwtUserDetails userDetails = new JwtUserDetails(email, userId);
                        JwtAuthenticationToken authentication = new JwtAuthenticationToken(userDetails);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }

                    break; // No need to process further cookies
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
