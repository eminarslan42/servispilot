package com.vehicle.car.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vehicle.car.model.User;
import com.vehicle.car.repository.UserRepository;
import com.vehicle.car.security.services.UserDetailsImpl;
import com.vehicle.car.service.SubscriptionService;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private SubscriptionService subscriptionService;
    
    @GetMapping
    public String showProfile(Model model) {
        // Giriş yapmış kullanıcının bilgilerini al
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // Kullanıcı bilgilerini veritabanından çek
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        
        // Abonelik durumunu kontrol et
        boolean isSubscriptionValid = subscriptionService.isSubscriptionValid(user.getId());
        
        // Layout için gerekli model değişkenlerini ekle
        model.addAttribute("isAuthenticated", true);
        model.addAttribute("username", user.getUsername());
        
        // Profil sayfası için gerekli model değişkenlerini ekle
        model.addAttribute("user", user);
        model.addAttribute("isSubscriptionValid", isSubscriptionValid);
        
        return "profile/index";
    }
    
    @PostMapping("/update")
    public String updateProfile(@ModelAttribute("user") User updatedUser, RedirectAttributes redirectAttributes) {
        // Giriş yapmış kullanıcının bilgilerini al
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // Kullanıcı bilgilerini veritabanından çek
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        
        // Sadece izin verilen alanları güncelle
        user.setUsername(updatedUser.getUsername());
        user.setEmail(updatedUser.getEmail());
        user.setFirstName(updatedUser.getFirstName());
        user.setLastName(updatedUser.getLastName());
        user.setPhone(updatedUser.getPhone());
        
        // Kullanıcıyı kaydet
        userRepository.save(user);
        
        redirectAttributes.addFlashAttribute("successMessage", "Profil bilgileriniz başarıyla güncellendi.");
        
        return "redirect:/profile";
    }
    
    @PostMapping("/change-password")
    public String changePassword(String currentPassword, String newPassword, String confirmPassword, 
                                RedirectAttributes redirectAttributes) {
        // Giriş yapmış kullanıcının bilgilerini al
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // Kullanıcı bilgilerini veritabanından çek
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        
        // Mevcut şifre kontrolü
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("passwordError", "Mevcut şifreniz yanlış.");
            return "redirect:/profile";
        }
        
        // Yeni şifre ve onay kontrolü
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", "Yeni şifre ve onay şifresi eşleşmiyor.");
            return "redirect:/profile";
        }
        
        // Şifreyi güncelle
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        redirectAttributes.addFlashAttribute("passwordSuccess", "Şifreniz başarıyla güncellendi.");
        
        return "redirect:/profile";
    }
} 