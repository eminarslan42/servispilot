package com.vehicle.car.security.jwt;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.vehicle.car.security.services.UserDetailsServiceImpl;
import com.vehicle.car.service.SubscriptionService;
import com.vehicle.car.security.services.UserDetailsImpl;

public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @Autowired
    private SubscriptionService subscriptionService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Skip filter for non-API requests if authentication already exists
            if (!isApiRequest(request.getRequestURI()) && SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }
            
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // Check subscription status for protected endpoints
                if (!isPublicEndpoint(request.getRequestURI())) {
                    UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetails;
                    if (!subscriptionService.isSubscriptionValid(userDetailsImpl.getId())) {
                        response.setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED);
                        response.getWriter().write("Subscription expired or inactive");
                        return;
                    }
                }
                
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
    
    private boolean isPublicEndpoint(String uri) {
        return uri.contains("/api/auth/") || // Auth endpoints
               uri.contains("/api/public/") || // Public endpoints if any
               uri.equals("/") || // Root path
               uri.equals("/login") || // Login page
               uri.equals("/register"); // Register page
    }
    
    private boolean isApiRequest(String uri) {
        return uri.startsWith("/api/");
    }
} 