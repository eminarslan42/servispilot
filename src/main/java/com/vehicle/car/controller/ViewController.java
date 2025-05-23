package com.vehicle.car.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViewController {

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "subscription", required = false) String subscription,
                        Model model) {
        
        if (error != null) {
            model.addAttribute("error", "Geçersiz kullanıcı adı veya şifre");
        }
        
        if (logout != null) {
            model.addAttribute("message", "Başarıyla çıkış yaptınız");
        }
        
        if (subscription != null) {
            model.addAttribute("error", "Aboneliğiniz bitti, lütfen servispilot@hotmail.com ile iletişime geçiniz.");
        }
        
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        // Kullanıcının admin olup olmadığını kontrol et
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = false;
        
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getPrincipal())) {
            isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        
        model.addAttribute("isAdmin", isAdmin);
        return "auth/register";
    }
} 