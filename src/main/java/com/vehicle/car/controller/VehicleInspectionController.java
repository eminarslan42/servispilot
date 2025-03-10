package com.vehicle.car.controller;

import com.vehicle.car.model.VehicleInspection;
import com.vehicle.car.model.Vehicle;
import com.vehicle.car.service.VehicleInspectionService;
import com.vehicle.car.service.VehicleService;
import com.vehicle.car.service.PDFService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/inspections")
public class VehicleInspectionController {
    private final VehicleInspectionService vehicleInspectionService;
    private final VehicleService vehicleService;
    private final PDFService pdfService;

    @GetMapping
    public String listInspections(Model model) {
        model.addAttribute("inspections", vehicleInspectionService.getAllInspections());
        return "inspection/list";
    }

    @GetMapping("/new")
    public String showInspectionForm(@RequestParam(required = false) Long vehicleId, Model model) {
        try {
            VehicleInspection inspection = new VehicleInspection();
            
            // Varsayılan değerleri ayarla
            inspection.setExteriorCondition(0);
            inspection.setInteriorCondition(0);
            inspection.setMechanicalCondition(0);
            inspection.setHasSpareWheel(false);
            inspection.setHasJack(false);
            inspection.setHasFirstAidKit(false);
            inspection.setHasWarningTriangle(false);
            inspection.setDamageData("[]");
            
            // Araç ID'si varsa, aracı bul ve ekspertize ekle
            if (vehicleId != null) {
                Optional<Vehicle> vehicleOpt = vehicleService.getVehicleById(vehicleId);
                if (vehicleOpt.isPresent()) {
                    Vehicle vehicle = vehicleOpt.get();
                    inspection.setVehicle(vehicle);
                    
                    // Araçtan kilometre bilgisini al
                    if (vehicle.getCurrentKilometer() != null) {
                        inspection.setCurrentKilometer(vehicle.getCurrentKilometer());
                    }
                }
            }
            
            model.addAttribute("inspection", inspection);
            model.addAttribute("vehicles", vehicleService.getAllVehicles());
            return "inspection/form";
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ekspertiz formu yüklenirken bir hata oluştu: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public String viewInspection(@PathVariable Long id, Model model) {
        vehicleInspectionService.getInspectionById(id).ifPresent(inspection -> {
            model.addAttribute("inspection", inspection);
        });
        return "inspection/view";
    }

    @GetMapping("/{id}/edit")
    public String editInspection(@PathVariable Long id, Model model) {
        vehicleInspectionService.getInspectionById(id).ifPresent(inspection -> {
            // Null değerleri kontrol et
            if (inspection.getExteriorCondition() == null) {
                inspection.setExteriorCondition(0);
            }
            if (inspection.getInteriorCondition() == null) {
                inspection.setInteriorCondition(0);
            }
            if (inspection.getMechanicalCondition() == null) {
                inspection.setMechanicalCondition(0);
            }
            if (inspection.getHasSpareWheel() == null) {
                inspection.setHasSpareWheel(false);
            }
            if (inspection.getHasJack() == null) {
                inspection.setHasJack(false);
            }
            if (inspection.getHasFirstAidKit() == null) {
                inspection.setHasFirstAidKit(false);
            }
            if (inspection.getHasWarningTriangle() == null) {
                inspection.setHasWarningTriangle(false);
            }
            if (inspection.getDamageData() == null) {
                inspection.setDamageData("[]");
            }
            
            model.addAttribute("inspection", inspection);
            model.addAttribute("vehicles", vehicleService.getAllVehicles());
        });
        return "inspection/form";
    }

    @PostMapping
    public String saveInspection(@ModelAttribute VehicleInspection inspection) {
        // Null kontrolü
        if (inspection.getExteriorCondition() != null && inspection.getExteriorCondition() == 0) {
            inspection.setExteriorCondition(null);
        }
        if (inspection.getInteriorCondition() != null && inspection.getInteriorCondition() == 0) {
            inspection.setInteriorCondition(null);
        }
        if (inspection.getMechanicalCondition() != null && inspection.getMechanicalCondition() == 0) {
            inspection.setMechanicalCondition(null);
        }
        
        vehicleInspectionService.saveInspection(inspection);
        return "redirect:/vehicles/" + inspection.getVehicle().getId();
    }

    @PostMapping("/{id}")
    public String updateInspection(@PathVariable Long id, @ModelAttribute VehicleInspection inspection) {
        inspection.setId(id); // Ensure the ID is set
        
        // Null kontrolü
        if (inspection.getExteriorCondition() != null && inspection.getExteriorCondition() == 0) {
            inspection.setExteriorCondition(null);
        }
        if (inspection.getInteriorCondition() != null && inspection.getInteriorCondition() == 0) {
            inspection.setInteriorCondition(null);
        }
        if (inspection.getMechanicalCondition() != null && inspection.getMechanicalCondition() == 0) {
            inspection.setMechanicalCondition(null);
        }
        
        vehicleInspectionService.saveInspection(inspection);
        return "redirect:/vehicles/" + inspection.getVehicle().getId();
    }

    @PostMapping("/{id}/delete")
    public String deleteInspection(@PathVariable Long id) {
        VehicleInspection inspection = vehicleInspectionService.getInspectionById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid inspection Id:" + id));
        Long vehicleId = inspection.getVehicle().getId();
        vehicleInspectionService.deleteInspection(id);
        return "redirect:/vehicles/" + vehicleId;
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadInspectionPDF(@PathVariable Long id) {
        VehicleInspection inspection = vehicleInspectionService.getInspectionById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid inspection Id:" + id));
        
        byte[] pdfBytes = pdfService.generateInspectionReport(inspection);
        
        String fileName = String.format("ekspertiz_raporu_%s_%s.pdf", 
            inspection.getVehicle().getPlate().replace(" ", ""), 
            inspection.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", fileName);
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(pdfBytes);
    }
} 