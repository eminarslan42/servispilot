package com.vehicle.car.controller;

import com.vehicle.car.model.VehicleInspection;
import com.vehicle.car.model.Vehicle;
import com.vehicle.car.model.User;
import com.vehicle.car.service.VehicleInspectionService;
import com.vehicle.car.service.VehicleService;
import com.vehicle.car.service.PDFService;
import com.vehicle.car.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Optional;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/inspections")
public class VehicleInspectionController {
    private final VehicleInspectionService vehicleInspectionService;
    private final VehicleService vehicleService;
    private final PDFService pdfService;
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
    public String listInspections(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            if (currentUser.getRole().toString().equals("ROLE_ADMIN")) {
                // Admin kullanıcı tüm ekspertizleri görebilir
                model.addAttribute("inspections", vehicleInspectionService.getAllInspections());
            } else {
                // Normal kullanıcı sadece kendi ekspertizlerini görebilir (user ID'ye göre)
                List<VehicleInspection> allInspections = vehicleInspectionService.getAllInspections();
                List<VehicleInspection> userInspections = allInspections.stream()
                    .filter(inspection -> inspection.getUser() != null && 
                            inspection.getUser().getId().equals(currentUser.getId()))
                    .toList();
                model.addAttribute("inspections", userInspections);
            }
        } else {
            model.addAttribute("inspections", List.of());
        }
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
            
            // Mevcut kullanıcı bilgilerini ekle
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                inspection.setInspector(currentUser.getUsername());
                inspection.setUser(currentUser);
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
        Optional<VehicleInspection> inspectionOpt = vehicleInspectionService.getInspectionById(id);
        if (inspectionOpt.isPresent()) {
            VehicleInspection inspection = inspectionOpt.get();
            User currentUser = getCurrentUser();
            
            // Kullanıcı kontrolü - sadece kendi ekspertizlerini veya admin ise hepsini görebilir
            if (currentUser != null && (currentUser.getRole().toString().equals("ROLE_ADMIN") || 
                (inspection.getUser() != null && inspection.getUser().getId().equals(currentUser.getId())))) {
                model.addAttribute("inspection", inspection);
                return "inspection/view";
            }
        }
        return "redirect:/inspections?error=Yetkilendirme hatası";
    }

    @GetMapping("/{id}/edit")
    public String editInspection(@PathVariable Long id, Model model) {
        Optional<VehicleInspection> inspectionOpt = vehicleInspectionService.getInspectionById(id);
        if (inspectionOpt.isPresent()) {
            VehicleInspection inspection = inspectionOpt.get();
            User currentUser = getCurrentUser();
            
            // Kullanıcı kontrolü - sadece kendi ekspertizlerini veya admin ise hepsini düzenleyebilir
            if (currentUser != null && (currentUser.getRole().toString().equals("ROLE_ADMIN") || 
                (inspection.getUser() != null && inspection.getUser().getId().equals(currentUser.getId())))) {
                
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
                return "inspection/form";
            }
        }
        return "redirect:/inspections?error=Yetkilendirme hatası";
    }

    @PostMapping
    public String saveInspection(@ModelAttribute VehicleInspection inspection) {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            // Kullanıcı bilgilerini ekle
            inspection.setUser(currentUser);
            
            // Eğer inspector alanı boşsa, mevcut kullanıcının adını ata
            if (inspection.getInspector() == null || inspection.getInspector().isEmpty()) {
                inspection.setInspector(currentUser.getUsername());
            }
            
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
        return "redirect:/inspections?error=Yetkilendirme hatası";
    }

    @PostMapping("/{id}")
    public String updateInspection(@PathVariable Long id, @ModelAttribute VehicleInspection inspection) {
        Optional<VehicleInspection> existingInspectionOpt = vehicleInspectionService.getInspectionById(id);
        if (existingInspectionOpt.isPresent()) {
            VehicleInspection existingInspection = existingInspectionOpt.get();
            User currentUser = getCurrentUser();
            
            // Kullanıcı kontrolü - sadece kendi ekspertizlerini veya admin ise hepsini güncelleyebilir
            if (currentUser != null && (currentUser.getRole().toString().equals("ROLE_ADMIN") || 
                (existingInspection.getUser() != null && existingInspection.getUser().getId().equals(currentUser.getId())))) {
                
                inspection.setId(id); // Ensure the ID is set
                
                // Kullanıcı bilgilerini koru
                if (!currentUser.getRole().toString().equals("ROLE_ADMIN")) {
                    inspection.setUser(currentUser);
                    
                    // Eğer inspector alanı boşsa, mevcut kullanıcının adını ata
                    if (inspection.getInspector() == null || inspection.getInspector().isEmpty()) {
                        inspection.setInspector(currentUser.getUsername());
                    }
                } else if (existingInspection.getUser() != null) {
                    inspection.setUser(existingInspection.getUser());
                }
                
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
        }
        return "redirect:/inspections?error=Yetkilendirme hatası";
    }

    @PostMapping("/{id}/delete")
    public String deleteInspection(@PathVariable Long id) {
        Optional<VehicleInspection> inspectionOpt = vehicleInspectionService.getInspectionById(id);
        if (inspectionOpt.isPresent()) {
            VehicleInspection inspection = inspectionOpt.get();
            User currentUser = getCurrentUser();
            
            // Kullanıcı kontrolü - sadece kendi ekspertizlerini veya admin ise hepsini silebilir
            if (currentUser != null && (currentUser.getRole().toString().equals("ROLE_ADMIN") || 
                (inspection.getUser() != null && inspection.getUser().getId().equals(currentUser.getId())))) {
                
                Long vehicleId = inspection.getVehicle().getId();
                vehicleInspectionService.deleteInspection(id);
                return "redirect:/vehicles/" + vehicleId;
            }
        }
        return "redirect:/inspections?error=Yetkilendirme hatası";
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadInspectionPDF(@PathVariable Long id) {
        Optional<VehicleInspection> inspectionOpt = vehicleInspectionService.getInspectionById(id);
        if (inspectionOpt.isPresent()) {
            VehicleInspection inspection = inspectionOpt.get();
            User currentUser = getCurrentUser();
            
            // Kullanıcı kontrolü - sadece kendi ekspertizlerini veya admin ise hepsini indirebilir
            if (currentUser != null && (currentUser.getRole().toString().equals("ROLE_ADMIN") || 
                (inspection.getUser() != null && inspection.getUser().getId().equals(currentUser.getId())))) {
                
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
        return ResponseEntity.notFound().build();
    }
} 