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
import com.vehicle.car.service.UserService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
    public String listVehicles(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            if (user != null) {
                model.addAttribute("vehicles", vehicleService.getVehiclesByUserId(user.getId()));
            } else {
                model.addAttribute("vehicles", vehicleService.getAllVehicles());
            }
        } else {
            model.addAttribute("vehicles", vehicleService.getAllVehicles());
        }
        return "vehicle/list";
    }

    @GetMapping("/new")
    public String showVehicleForm(Model model) {
        model.addAttribute("vehicle", new Vehicle());
        return "vehicle/form";
    }

    @PostMapping
    public String saveVehicle(@ModelAttribute Vehicle vehicle) {
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
        return "redirect:/vehicles";
    }

    @GetMapping("/{id}")
    public String viewVehicle(@PathVariable Long id, Model model) {
        vehicleService.getVehicleById(id).ifPresent(vehicle -> {
            model.addAttribute("vehicle", vehicle);
            model.addAttribute("serviceRecords", serviceRecordService.getServiceRecordsByVehicleId(id));
            model.addAttribute("inspections", vehicleInspectionService.getInspectionsByVehicleId(id));
            model.addAttribute("diagnoses", faultDiagnosisService.getDiagnosesByVehicleId(id));
            model.addAttribute("newServiceRecord", new ServiceRecord());
        });
        return "vehicle/view";
    }

    @GetMapping("/search")
    public String searchVehicles(@RequestParam String query, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            if (user != null) {
                model.addAttribute("vehicles", vehicleService.searchVehiclesByUser(query, user.getId()));
            } else {
                model.addAttribute("vehicles", vehicleService.searchVehicles(query));
            }
        } else {
            model.addAttribute("vehicles", vehicleService.searchVehicles(query));
        }
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
} 