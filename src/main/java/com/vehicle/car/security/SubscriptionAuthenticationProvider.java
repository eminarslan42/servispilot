package com.vehicle.car.security;

import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.vehicle.car.security.services.UserDetailsImpl;
import com.vehicle.car.security.services.UserDetailsServiceImpl;
import com.vehicle.car.service.SubscriptionService;


@Component
public class SubscriptionAuthenticationProvider extends DaoAuthenticationProvider {
    
    private final SubscriptionService subscriptionService;
    
    public SubscriptionAuthenticationProvider(
            SubscriptionService subscriptionService,
            UserDetailsServiceImpl userDetailsService,
            PasswordEncoder passwordEncoder) {
        this.subscriptionService = subscriptionService;
        
        setUserDetailsService(userDetailsService);
        setPasswordEncoder(passwordEncoder);
    }
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            // First, authenticate with the standard DaoAuthenticationProvider
            Authentication auth = super.authenticate(authentication);
            
            // If authentication is successful, check subscription status
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetailsImpl) {
                UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
                
                // Check if the user has an active subscription
                if (!subscriptionService.isSubscriptionValid(userDetails.getId())) {
                    throw new SubscriptionExpiredException("Aboneliğiniz bitti, lütfen servispilot@hotmail.com ile iletişime geçiniz.");
                }
            }
            
            return auth;
        } catch (AccountStatusException e) {
            // Rethrow account status exceptions (like DisabledException)
            throw e;
        } catch (BadCredentialsException e) {
            // Rethrow bad credentials exception
            throw e;
        }
    }
    
    // Custom exception for subscription expiration
    public static class SubscriptionExpiredException extends DisabledException {
        public SubscriptionExpiredException(String msg) {
            super(msg);
        }
    }
} 