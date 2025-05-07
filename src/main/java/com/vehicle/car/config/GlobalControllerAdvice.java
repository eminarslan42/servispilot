package com.vehicle.car.config;

import com.vehicle.car.model.User;
import com.vehicle.car.service.ServiceReminderService;
import com.vehicle.car.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.vehicle.car.security.services.UserDetailsImpl;
import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {
    private final ServiceReminderService serviceReminderService;
    private final UserService userService;

    /**
     * Tüm hatırlatıcıları getirir (sayfa listelemeleri için)
     */
    @ModelAttribute("allActiveReminders")
    public Object getAllActiveReminders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                if (user.getRole().toString().equals("ROLE_ADMIN")) {
                    // Admin kullanıcılar tüm hatırlatmaları görebilir
                    return serviceReminderService.getActiveReminders();
                } else {
                    // Normal kullanıcılar sadece kendi hatırlatmalarını görebilir
                    return serviceReminderService.getActiveRemindersByUserId(user.getId());
                }
            }
        }
        // Kimlik doğrulama yoksa veya kullanıcı bulunamadıysa boş liste döndür
        return List.of();
    }
    
    /**
     * Sadece yaklaşan hatırlatıcıları getirir (zil bildirimi için)
     * Bugünden itibaren 1 gün içinde olan hatırlatmalar
     */
    @ModelAttribute("activeReminders")
    public Object getActiveReminders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                if (user.getRole().toString().equals("ROLE_ADMIN")) {
                    // Admin kullanıcılar yaklaşan tüm hatırlatmaları görebilir
                    return serviceReminderService.getUpcomingReminders();
                } else {
                    // Normal kullanıcılar sadece kendi yaklaşan hatırlatmalarını görebilir
                    return serviceReminderService.getUpcomingRemindersByUserId(user.getId());
                }
            }
        }
        // Kimlik doğrulama yoksa veya kullanıcı bulunamadıysa boş liste döndür
        return List.of();
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