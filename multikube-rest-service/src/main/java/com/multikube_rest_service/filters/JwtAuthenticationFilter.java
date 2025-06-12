package com.multikube_rest_service.filters;

import com.multikube_rest_service.auth.JwtAuthenticationToken;
import com.multikube_rest_service.auth.JwtUserDetails;
import com.multikube_rest_service.auth.JwtUtil;
import com.multikube_rest_service.common.HttpConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections; // Import Collections
import java.util.List;
import java.util.stream.Collectors;

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
            String[] cookies = cookieHeader.split(";");
            String jwtToken = null;

            for (String cookie : cookies) {
                String[] cookiePair = cookie.trim().split("=", 2);
                if (cookiePair.length == 2 && HttpConstants.JWT_TOKEN.equals(cookiePair[0])) {
                    jwtToken = cookiePair[1];
                    break;
                }
            }

            if (jwtToken != null && jwtUtil.validateToken(jwtToken)) {
                String email = jwtUtil.extractEmail(jwtToken);
                Long userId = jwtUtil.extractClaim(jwtToken, "userId", Long.class);
                Long tenantId = jwtUtil.extractClaim(jwtToken, "tenantId", Long.class);
                List<String> rolesClaim = jwtUtil.extractRoles(jwtToken); // Extract roles from token

                List<SimpleGrantedAuthority> authorities;
                if (rolesClaim != null) { // Check if the roles claim is not null
                    authorities = rolesClaim.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                } else {
                    authorities = Collections.emptyList(); // Default to an empty list if no roles claim
                }

                JwtUserDetails userDetails = new JwtUserDetails(email, userId, tenantId, authorities);
                JwtAuthenticationToken authentication = new JwtAuthenticationToken(userDetails);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}