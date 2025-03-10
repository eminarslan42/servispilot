package com.vehicle.car.controller;

import com.vehicle.car.model.VehicleMaintenanceHistory;
import com.vehicle.car.service.VehicleMaintenanceService;
import com.vehicle.car.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/maintenance")
public class VehicleMaintenanceController {
    
    private final VehicleMaintenanceService maintenanceService;
    private final VehicleService vehicleService;

    @GetMapping
    public String listAllMaintenance(Model model) {
        // Tüm bakım kayıtlarını getir
        List<VehicleMaintenanceHistory> allMaintenance = maintenanceService.getAllMaintenanceRecords();
        model.addAttribute("maintenanceRecords", allMaintenance);
        return "maintenance/list";
    }

    @GetMapping("/vehicle/{vehicleId}")
    public String listVehicleMaintenance(@PathVariable Long vehicleId, Model model) {
        vehicleService.getVehicleById(vehicleId).ifPresent(vehicle -> {
            model.addAttribute("vehicle", vehicle);
            model.addAttribute("maintenanceHistory", maintenanceService.getVehicleMaintenanceHistory(vehicleId));
            model.addAttribute("warrantyServices", maintenanceService.getWarrantyServices(vehicleId));
            model.addAttribute("needsMaintenance", maintenanceService.isMaintenanceNeeded(vehicle));
        });
        return "maintenance/vehicle-history";
    }

    @GetMapping("/new")
    public String showMaintenanceForm(@RequestParam Long vehicleId, Model model) {
        VehicleMaintenanceHistory maintenance = new VehicleMaintenanceHistory();
        vehicleService.getVehicleById(vehicleId).ifPresent(maintenance::setVehicle);
        
        model.addAttribute("maintenance", maintenance);
        return "maintenance/form";
    }

    @GetMapping("/{id}")
    public String viewMaintenance(@PathVariable Long id, Model model) {
        maintenanceService.getMaintenanceRecord(id).ifPresent(maintenance -> {
            model.addAttribute("maintenance", maintenance);
        });
        return "maintenance/view";
    }

    @GetMapping("/{id}/edit")
    public String editMaintenance(@PathVariable Long id, Model model) {
        maintenanceService.getMaintenanceRecord(id).ifPresent(maintenance -> {
            model.addAttribute("maintenance", maintenance);
        });
        return "maintenance/form";
    }

    @PostMapping
    public String saveMaintenance(@ModelAttribute VehicleMaintenanceHistory maintenance) {
        maintenanceService.addMaintenanceRecord(maintenance);
        return "redirect:/maintenance/vehicle/" + maintenance.getVehicle().getId();
    }

    @PostMapping("/{id}")
    public String updateMaintenance(@PathVariable Long id, @ModelAttribute VehicleMaintenanceHistory maintenance) {
        maintenanceService.updateMaintenanceRecord(id, maintenance);
        return "redirect:/maintenance/vehicle/" + maintenance.getVehicle().getId();
    }

    @PostMapping("/{id}/delete")
    public String deleteMaintenance(@PathVariable Long id) {
        VehicleMaintenanceHistory maintenance = maintenanceService.getMaintenanceRecord(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid maintenance Id:" + id));
        Long vehicleId = maintenance.getVehicle().getId();
        maintenanceService.deleteMaintenanceRecord(id);
        return "redirect:/maintenance/vehicle/" + vehicleId;
    }

    @GetMapping("/report")
    public String showMaintenanceReport(
            @RequestParam Long vehicleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Model model) {
        
        vehicleService.getVehicleById(vehicleId).ifPresent(vehicle -> {
            model.addAttribute("vehicle", vehicle);
            model.addAttribute("maintenanceHistory", 
                maintenanceService.getMaintenanceHistoryByDateRange(vehicleId, startDate, endDate));
            model.addAttribute("totalCost", 
                maintenanceService.calculateTotalMaintenanceCost(vehicleId, startDate, endDate));
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
        });
        
        return "maintenance/report";
    }
} 