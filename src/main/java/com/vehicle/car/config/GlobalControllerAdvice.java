package com.vehicle.car.config;

import com.vehicle.car.service.ServiceReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.vehicle.car.security.services.UserDetailsImpl;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {
    private final ServiceReminderService serviceReminderService;

    @ModelAttribute("activeReminders")
    public Object getActiveReminders() {
        return serviceReminderService.getActiveReminders();
    }
    
    @ModelAttribute("currentUser")
    public Authentication getCurrentUser() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
    
    @ModelAttribute("isAuthenticated")
    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
    }
    
    @ModelAttribute("username")
    public String getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            if (auth.getPrincipal() instanceof UserDetailsImpl) {
                return ((UserDetailsImpl) auth.getPrincipal()).getUsername();
            }
            return auth.getName();
        }
        return null;
    }
} 