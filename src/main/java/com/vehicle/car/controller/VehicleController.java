package com.vehicle.car.controller;

import com.vehicle.car.model.Vehicle;
import com.vehicle.car.model.ServiceRecord;
import com.vehicle.car.model.User;
import com.vehicle.car.service.VehicleService;
import com.vehicle.car.service.ServiceRecordService;
import com.vehicle.car.service.VehicleInspectionService;
import com.vehicle.car.service.FaultDiagnosisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.vehicle.car.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/vehicles")
public class VehicleController {
    private final VehicleService vehicleService;
    private final ServiceRecordService serviceRecordService;
    private final VehicleInspectionService vehicleInspectionService;
    private final FaultDiagnosisService faultDiagnosisService;
    private final UserService userService;

    @GetMapping
    public String listVehicles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).descending());
        Page<Vehicle> vehiclePage;
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            if (user != null) {
                vehiclePage = vehicleService.getVehiclesByUserIdPaginated(user.getId(), pageable);
            } else {
                vehiclePage = vehicleService.getAllVehiclesPaginated(pageable);
            }
        } else {
            vehiclePage = vehicleService.getAllVehiclesPaginated(pageable);
        }
        
        List<Vehicle> vehicles = vehiclePage.getContent();
        
        // Her bir araç için servis kayıtlarını en yeni tarihli olacak şekilde güncelle
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getServiceRecords() != null && !vehicle.getServiceRecords().isEmpty()) {
                List<ServiceRecord> sortedRecords = serviceRecordService.getLatestServiceRecordsByVehicleId(vehicle.getId());
                vehicle.setServiceRecords(sortedRecords);
            }
        }
        
        model.addAttribute("vehicles", vehicles);
        model.addAttribute("currentPage", vehiclePage.getNumber());
        model.addAttribute("totalPages", Math.max(1, vehiclePage.getTotalPages()));
        model.addAttribute("totalItems", vehiclePage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortField", sort);
        
        return "vehicle/list";
    }

    @GetMapping("/new")
    public String showVehicleForm(Model model) {
        model.addAttribute("vehicle", new Vehicle());
        return "vehicle/form";
    }

    @PostMapping
    public String saveVehicle(@ModelAttribute Vehicle vehicle, RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                User user = userService.findByUsername(userDetails.getUsername());
                if (user != null) {
                    vehicleService.saveVehicleForUser(vehicle, user.getId());
                } else {
                    vehicleService.saveVehicle(vehicle);
                }
            } else {
                vehicleService.saveVehicle(vehicle);
            }
            redirectAttributes.addFlashAttribute("success", "Araç başarıyla kaydedildi");
            return "redirect:/vehicles";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("vehicle", vehicle);
            return "redirect:/vehicles/new";
        }
    }

    @GetMapping("/{id}")
    public String viewVehicle(@PathVariable Long id, Model model) {
        vehicleService.getVehicleById(id).ifPresent(vehicle -> {
            model.addAttribute("vehicle", vehicle);
            
            // Servis kayıtları (en yeni tarihli sıralı)
            List<ServiceRecord> allServiceRecords = serviceRecordService.getLatestServiceRecordsByVehicleId(id);
            model.addAttribute("serviceRecords", allServiceRecords);
            
            // Son servis kaydı
            serviceRecordService.getLastServiceRecordByVehicleId(id)
                .ifPresent(lastRecord -> model.addAttribute("lastServiceRecord", lastRecord));
            
            // Ekspertiz kayıtları
            model.addAttribute("inspections", vehicleInspectionService.getInspectionsByVehicleId(id));
            
            // Arıza teşhisleri
            model.addAttribute("diagnoses", faultDiagnosisService.getDiagnosesByVehicleId(id));
            
            model.addAttribute("newServiceRecord", new ServiceRecord());
        });
        return "vehicle/view";
    }

    @GetMapping("/search")
    public String searchVehicles(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).descending());
        Page<Vehicle> vehiclePage;
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            if (user != null) {
                vehiclePage = vehicleService.searchVehiclesByUserPaginated(query, user.getId(), pageable);
            } else {
                vehiclePage = vehicleService.searchVehiclesPaginated(query, pageable);
            }
        } else {
            vehiclePage = vehicleService.searchVehiclesPaginated(query, pageable);
        }
        
        List<Vehicle> vehicles = vehiclePage.getContent();
        
        // Her bir araç için servis kayıtlarını en yeni tarihli olacak şekilde güncelle
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getServiceRecords() != null && !vehicle.getServiceRecords().isEmpty()) {
                List<ServiceRecord> sortedRecords = serviceRecordService.getLatestServiceRecordsByVehicleId(vehicle.getId());
                vehicle.setServiceRecords(sortedRecords);
            }
        }
        
        model.addAttribute("vehicles", vehicles);
        model.addAttribute("currentPage", vehiclePage.getNumber());
        model.addAttribute("totalPages", Math.max(1, vehiclePage.getTotalPages()));
        model.addAttribute("totalItems", vehiclePage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortField", sort);
        model.addAttribute("searchQuery", query);
        
        return "vehicle/list";
    }

    @PostMapping("/{id}/delete")
    public String deleteVehicle(@PathVariable Long id) {
        try {
            Vehicle vehicle = vehicleService.getVehicleById(id)
                .orElseThrow(() -> new IllegalStateException("Araç bulunamadı"));
            
            // Check if the current user is the owner of the vehicle
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                User user = userService.findByUsername(userDetails.getUsername());
                if (user != null && vehicle.getUser() != null && !vehicle.getUser().getId().equals(user.getId())) {
                    return "redirect:/vehicles?error=" + URLEncoder.encode("Bu aracı silme yetkiniz yok", StandardCharsets.UTF_8);
                }
            }
            
            vehicleService.deleteVehicle(id);
            return "redirect:/vehicles";
        } catch (IllegalStateException e) {
            return "redirect:/vehicles?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }
    }

    @PostMapping("/{id}/update")
    @ResponseBody
    public ResponseEntity<?> updateVehicle(@PathVariable Long id, @RequestBody Map<String, Object> vehicleData) {
        try {
            // Get the vehicle from the database
            Optional<Vehicle> optionalVehicle = vehicleService.getVehicleById(id);
            if (!optionalVehicle.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Araç bulunamadı");
            }
            
            Vehicle vehicle = optionalVehicle.get();
            
            // Check if the current user is the owner of the vehicle or admin
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                User user = userService.findByUsername(userDetails.getUsername());
                
                // Only allow the owner or admin to update
                if (user != null && vehicle.getUser() != null && 
                    !vehicle.getUser().getId().equals(user.getId()) && 
                    !user.getRole().toString().equals("ROLE_ADMIN")) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Bu aracı düzenleme yetkiniz yok");
                }
            }
            
            // Update vehicle properties from the request data
            if (vehicleData.containsKey("licensePlate")) {
                vehicle.setPlate((String) vehicleData.get("licensePlate"));
            }
            
            if (vehicleData.containsKey("make")) {
                vehicle.setBrand((String) vehicleData.get("make"));
            }
            
            if (vehicleData.containsKey("model")) {
                vehicle.setModel((String) vehicleData.get("model"));
            }
            
            if (vehicleData.containsKey("year")) {
                String yearStr = vehicleData.get("year").toString();
                try {
                    int year = Integer.parseInt(yearStr);
                    vehicle.setYear(year);
                } catch (NumberFormatException e) {
                    // Ignore if not a valid number
                }
            }
            
            if (vehicleData.containsKey("ownerName")) {
                vehicle.setOwnerName((String) vehicleData.get("ownerName"));
            }
            
            if (vehicleData.containsKey("ownerPhone")) {
                vehicle.setOwnerPhone((String) vehicleData.get("ownerPhone"));
            }
            
            if (vehicleData.containsKey("currentKilometer")) {
                String kilometerStr = vehicleData.get("currentKilometer").toString();
                try {
                    int kilometer = Integer.parseInt(kilometerStr);
                    vehicle.setCurrentKilometer(kilometer);
                } catch (NumberFormatException e) {
                    // Ignore if not a valid number
                }
            }
            
            // Save the updated vehicle
            vehicleService.saveVehicle(vehicle);
            
            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Araç başarıyla güncellendi");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Araç güncellenirken bir hata oluştu: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/edit")
    public String editVehicle(@PathVariable Long id, @ModelAttribute Vehicle updatedVehicle, RedirectAttributes redirectAttributes) {
        try {
            Optional<Vehicle> optionalVehicle = vehicleService.getVehicleById(id);
            if (!optionalVehicle.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Araç bulunamadı");
                return "redirect:/vehicles";
            }
            
            Vehicle vehicle = optionalVehicle.get();
            
            // Check if the current user is the owner of the vehicle or admin
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                User user = userService.findByUsername(userDetails.getUsername());
                
                // Only allow the owner or admin to update
                if (user != null && vehicle.getUser() != null && 
                    !vehicle.getUser().getId().equals(user.getId()) && 
                    !user.getRole().toString().equals("ROLE_ADMIN")) {
                    redirectAttributes.addFlashAttribute("error", "Bu aracı düzenleme yetkiniz yok");
                    return "redirect:/vehicles";
                }
            }
            
            // Update the vehicle properties
            vehicle.setPlate(updatedVehicle.getPlate());
            vehicle.setBrand(updatedVehicle.getBrand());
            vehicle.setModel(updatedVehicle.getModel());
            vehicle.setYear(updatedVehicle.getYear());
            vehicle.setOwnerName(updatedVehicle.getOwnerName());
            vehicle.setOwnerPhone(updatedVehicle.getOwnerPhone());
            
            if (updatedVehicle.getCurrentKilometer() != null) {
                vehicle.setCurrentKilometer(updatedVehicle.getCurrentKilometer());
            }
            
            // Save the updated vehicle
            vehicleService.saveVehicle(vehicle);
            
            redirectAttributes.addFlashAttribute("success", "Araç başarıyla güncellendi");
            return "redirect:/vehicles";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Araç güncellenirken bir hata oluştu: " + e.getMessage());
            return "redirect:/vehicles";
        }
    }
} 