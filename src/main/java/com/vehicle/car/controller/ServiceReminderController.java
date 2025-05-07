package com.vehicle.car.controller;

import com.vehicle.car.model.ServiceReminder;
import com.vehicle.car.model.User;
import com.vehicle.car.service.ServiceReminderService;
import com.vehicle.car.service.VehicleService;
import com.vehicle.car.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reminders")
public class ServiceReminderController {
    private final ServiceReminderService serviceReminderService;
    private final VehicleService vehicleService;
    private final UserService userService;

    // Güncel kullanıcıyı alma yardımcı metodu
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userService.findByUsername(userDetails.getUsername());
        }
        return null;
    }

    @GetMapping
    public String listReminders(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            if (currentUser.getRole().toString().equals("ROLE_ADMIN")) {
                // Admin kullanıcı tüm hatırlatmaları görebilir
                model.addAttribute("reminders", serviceReminderService.getActiveReminders());
            } else {
                // Normal kullanıcı sadece kendi hatırlatmalarını görebilir
                model.addAttribute("reminders", serviceReminderService.getActiveRemindersByUserId(currentUser.getId()));
            }
        } else {
            model.addAttribute("reminders", List.of());
        }
        return "reminder/list";
    }

    @GetMapping("/new")
    public String showReminderForm(@RequestParam(required = false) Long vehicleId, Model model) {
        ServiceReminder reminder = new ServiceReminder();
        if (vehicleId != null) {
            vehicleService.getVehicleById(vehicleId).ifPresent(reminder::setVehicle);
        }
        
        // Mevcut kullanıcı bilgilerini ekle
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            reminder.setUser(currentUser);
        }
        
        model.addAttribute("reminder", reminder);
        model.addAttribute("vehicles", vehicleService.getAllVehicles());
        return "reminder/form";
    }

    @PostMapping
    public String saveReminder(@ModelAttribute ServiceReminder reminder) {
        // Kullanıcı bilgilerini ekle
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            reminder.setUser(currentUser);
            reminder.setStatus("Aktif");
            serviceReminderService.saveReminder(reminder);
            return "redirect:/vehicles/" + reminder.getVehicle().getId();
        }
        return "redirect:/reminders?error=Yetkilendirme hatası";
    }

    @PostMapping("/{id}/complete")
    public String completeReminder(@PathVariable Long id) {
        Optional<ServiceReminder> reminderOpt = serviceReminderService.getReminderById(id);
        if (reminderOpt.isPresent()) {
            ServiceReminder reminder = reminderOpt.get();
            User currentUser = getCurrentUser();
            
            // Kullanıcı kontrolü - sadece kendi hatırlatmalarını veya admin ise hepsini tamamlayabilir
            if (currentUser != null && (currentUser.getRole().toString().equals("ROLE_ADMIN") || 
                (reminder.getUser() != null && reminder.getUser().getId().equals(currentUser.getId())))) {
                
                reminder.setStatus("Tamamlandı");
                serviceReminderService.saveReminder(reminder);
                return "redirect:/reminders";
            }
        }
        return "redirect:/reminders?error=Yetkilendirme hatası";
    }

    @PostMapping("/{id}/cancel")
    public String cancelReminder(@PathVariable Long id) {
        Optional<ServiceReminder> reminderOpt = serviceReminderService.getReminderById(id);
        if (reminderOpt.isPresent()) {
            ServiceReminder reminder = reminderOpt.get();
            User currentUser = getCurrentUser();
            
            // Kullanıcı kontrolü - sadece kendi hatırlatmalarını veya admin ise hepsini iptal edebilir
            if (currentUser != null && (currentUser.getRole().toString().equals("ROLE_ADMIN") || 
                (reminder.getUser() != null && reminder.getUser().getId().equals(currentUser.getId())))) {
                
                reminder.setStatus("İptal");
                serviceReminderService.saveReminder(reminder);
                return "redirect:/reminders";
            }
        }
        return "redirect:/reminders?error=Yetkilendirme hatası";
    }

    @PostMapping("/{id}/delete")
    public String deleteReminder(@PathVariable Long id) {
        Optional<ServiceReminder> reminderOpt = serviceReminderService.getReminderById(id);
        if (reminderOpt.isPresent()) {
            ServiceReminder reminder = reminderOpt.get();
            User currentUser = getCurrentUser();
            
            // Kullanıcı kontrolü - sadece kendi hatırlatmalarını veya admin ise hepsini silebilir
            if (currentUser != null && (currentUser.getRole().toString().equals("ROLE_ADMIN") || 
                (reminder.getUser() != null && reminder.getUser().getId().equals(currentUser.getId())))) {
                
                serviceReminderService.deleteReminder(id);
                return "redirect:/reminders";
            }
        }
        return "redirect:/reminders?error=Yetkilendirme hatası";
    }
} 