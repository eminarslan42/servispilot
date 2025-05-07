package com.vehicle.car.controller;

import com.vehicle.car.model.ServiceRecord;
import com.vehicle.car.model.User;
import com.vehicle.car.service.ServiceRecordService;
import com.vehicle.car.service.VehicleService;
import com.vehicle.car.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
@RequestMapping("/service-records")
public class ServiceRecordController {
    private final ServiceRecordService serviceRecordService;
    private final VehicleService vehicleService;
    private final UserService userService;

    @PostMapping
    public String saveServiceRecord(@ModelAttribute ServiceRecord serviceRecord, @RequestParam Long vehicleId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final User currentUser;
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            currentUser = userService.findByUsername(userDetails.getUsername());
        } else {
            currentUser = null;
        }
        
        vehicleService.getVehicleById(vehicleId).ifPresent(vehicle -> {
            serviceRecord.setVehicle(vehicle);
            serviceRecord.setServiceDate(LocalDateTime.now());
            
            if (currentUser != null) {
                serviceRecord.setUser(currentUser);
            }
            
            serviceRecordService.saveServiceRecord(serviceRecord);
        });
        return "redirect:/vehicles/" + vehicleId;
    }

    @PostMapping("/{id}/update")
    public String updateServiceRecord(@PathVariable Long id, @ModelAttribute ServiceRecord serviceRecord) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User currentUser = userService.findByUsername(userDetails.getUsername());
            
            if (currentUser != null) {
                ServiceRecord existingRecord = serviceRecordService.getServiceRecordById(id).orElse(null);
                
                if (existingRecord != null) {
                    // Yetkilendirme kontrolü
                    if (existingRecord.getUser() != null && 
                        !existingRecord.getUser().getId().equals(currentUser.getId()) && 
                        !currentUser.getRole().toString().equals("ROLE_ADMIN")) {
                        return "redirect:/vehicles/" + existingRecord.getVehicle().getId() + 
                               "?error=" + URLEncoder.encode("Bu servis kaydini guncelleme yetkiniz yok", StandardCharsets.UTF_8);
                    }
                    
                    // Mevcut değerleri koru
                    serviceRecord.setId(id);
                    serviceRecord.setVehicle(existingRecord.getVehicle());
                    serviceRecord.setUser(existingRecord.getUser());
                    serviceRecord.setServiceDate(existingRecord.getServiceDate());
                    
                    serviceRecordService.saveServiceRecord(serviceRecord);
                    return "redirect:/vehicles/" + existingRecord.getVehicle().getId();
                }
            }
        }
        
        return "redirect:/dashboard";
    }

    @PostMapping("/{id}/delete")
    public String deleteServiceRecord(@PathVariable Long id, @RequestParam Long vehicleId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User currentUser = userService.findByUsername(userDetails.getUsername());
            
            if (currentUser != null) {
                ServiceRecord serviceRecord = serviceRecordService.getServiceRecordById(id).orElse(null);
                if (serviceRecord != null && serviceRecord.getUser() != null && 
                    !serviceRecord.getUser().getId().equals(currentUser.getId()) && 
                    !currentUser.getRole().toString().equals("ROLE_ADMIN")) {
                    return "redirect:/vehicles/" + vehicleId + 
                           "?error=" + URLEncoder.encode("Bu servis kaydini silme yetkiniz yok", StandardCharsets.UTF_8);
                }
            }
        }
        
        serviceRecordService.deleteServiceRecord(id);
        return "redirect:/vehicles/" + vehicleId;
    }
} 