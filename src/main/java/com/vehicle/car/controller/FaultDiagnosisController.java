package com.vehicle.car.controller;

import com.vehicle.car.model.FaultDiagnosis;
import com.vehicle.car.model.Vehicle;
import com.vehicle.car.model.User;
import com.vehicle.car.service.FaultDiagnosisService;
import com.vehicle.car.service.VehicleService;
import com.vehicle.car.service.UserService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/diagnoses")
public class FaultDiagnosisController {
    private final FaultDiagnosisService faultDiagnosisService;
    private final VehicleService vehicleService;
    private final UserService userService;

    // Mevcut kullanıcıyı al
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userService.findByUsername(userDetails.getUsername());
        }
        return null;
    }

    @GetMapping("/vehicle/{vehicleId}")
    public String listVehicleDiagnoses(@PathVariable Long vehicleId, Model model) {
        User currentUser = getCurrentUser();
        
        vehicleService.getVehicleById(vehicleId).ifPresent(vehicle -> {
            model.addAttribute("vehicle", vehicle);
            
            // Kullanıcının rolünü kontrol et
            if (currentUser != null && "ROLE_ADMIN".equals(currentUser.getRole().toString())) {
                // Admin kullanıcı ise tüm kayıtları göster
                model.addAttribute("diagnoses", faultDiagnosisService.getDiagnosesByVehicleId(vehicleId));
            } else if (currentUser != null) {
                // Normal kullanıcı ise sadece kendi kayıtlarını göster
                List<FaultDiagnosis> allDiagnoses = faultDiagnosisService.getDiagnosesByVehicleId(vehicleId);
                List<FaultDiagnosis> userDiagnoses = allDiagnoses.stream()
                    .filter(d -> d.getUser() != null && d.getUser().getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
                model.addAttribute("diagnoses", userDiagnoses);
            } else {
                // Giriş yapmamış kullanıcılara boş liste göster
                model.addAttribute("diagnoses", List.of());
            }
        });
        
        return "diagnosis/list";
    }

    @GetMapping("/new")
    public String showDiagnosisForm(@RequestParam Long vehicleId, Model model) {
        Vehicle vehicle = vehicleService.getVehicleById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Araç bulunamadı: " + vehicleId));

        FaultDiagnosis diagnosis = new FaultDiagnosis();
        diagnosis.setVehicle(vehicle);
        diagnosis.setCurrentKilometer(vehicle.getCurrentKilometer());
        diagnosis.setStatus("Yeni");
        
        // Mevcut kullanıcı bilgisini doldur
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            diagnosis.setDiagnosedBy(currentUser.getUsername());
            diagnosis.setUser(currentUser);
        }

        model.addAttribute("diagnosis", diagnosis);
        model.addAttribute("vehicle", vehicle);
        return "diagnosis/form";
    }

    @PostMapping
    public String saveDiagnosis(@ModelAttribute FaultDiagnosis diagnosis, @RequestParam Long vehicleId) {
        try {
            Vehicle vehicle = vehicleService.getVehicleById(vehicleId)
                    .orElseThrow(() -> new IllegalArgumentException("Araç bulunamadı"));
            
            diagnosis.setVehicle(vehicle);
            diagnosis.setStatus("Yeni");
            diagnosis.setDiagnosisDate(LocalDateTime.now());
            
            // Mevcut kullanıcıyı teşhis ile ilişkilendir
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                diagnosis.setUser(currentUser);
                // Eğer diagnosedBy alanı boşsa, mevcut kullanıcının adını ata
                if (diagnosis.getDiagnosedBy() == null || diagnosis.getDiagnosedBy().isEmpty()) {
                    diagnosis.setDiagnosedBy(currentUser.getUsername());
                }
            }
            
            faultDiagnosisService.saveDiagnosis(diagnosis);
            
            return "redirect:/vehicles/" + vehicleId;
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/vehicles/" + vehicleId + "?error=Arıza teşhisi kaydedilemedi";
        }
    }

    @GetMapping("/{id}")
    public String viewDiagnosis(@PathVariable Long id, Model model) {
        User currentUser = getCurrentUser();
        
        faultDiagnosisService.getDiagnosisById(id).ifPresent(diagnosis -> {
            // Kullanıcı admin değilse ve bu kayıt kullanıcıya ait değilse erişimi engelle
            if (currentUser != null && 
                !currentUser.getRole().toString().equals("ROLE_ADMIN") && 
                !diagnosis.getUser().getId().equals(currentUser.getId())) {
                model.addAttribute("error", "Bu teşhis kaydını görüntüleme yetkiniz yok.");
                return;
            }
            
            model.addAttribute("diagnosis", diagnosis);
        });
        
        return "diagnosis/view";
    }

    @PostMapping("/{id}/update-status")
    public String updateStatus(@PathVariable Long id, @RequestParam String status) {
        User currentUser = getCurrentUser();
        
        faultDiagnosisService.getDiagnosisById(id).ifPresent(diagnosis -> {
            // Kullanıcı admin değilse ve bu kayıt kullanıcıya ait değilse erişimi engelle
            if (currentUser != null && 
                !currentUser.getRole().toString().equals("ROLE_ADMIN") && 
                !diagnosis.getUser().getId().equals(currentUser.getId())) {
                return;
            }
            
            diagnosis.setStatus(status);
            
            // İptal durumunda standart ücret ayarla
            if ("İptal".equals(status)) {
                diagnosis.setEstimatedCost(800.0);
            }
            
            faultDiagnosisService.saveDiagnosis(diagnosis);
        });
        
        return "redirect:/diagnoses/" + id;
    }

    @GetMapping
    public String listAllDiagnoses(Model model) {
        User currentUser = getCurrentUser();
        
        if (currentUser != null) {
            if (currentUser.getRole().toString().equals("ROLE_ADMIN")) {
                // Admin kullanıcılar tüm teşhisleri görebilir
                model.addAttribute("diagnoses", faultDiagnosisService.getAllDiagnoses());
            } else {
                // Normal kullanıcılar sadece kendi eklediği teşhisleri görebilir
                List<FaultDiagnosis> allDiagnoses = faultDiagnosisService.getAllDiagnoses();
                List<FaultDiagnosis> userDiagnoses = allDiagnoses.stream()
                    .filter(d -> d.getUser() != null && d.getUser().getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
                model.addAttribute("diagnoses", userDiagnoses);
            }
        } else {
            // Giriş yapmamış kullanıcılara boş liste göster
            model.addAttribute("diagnoses", List.of());
        }
        
        model.addAttribute("vehicles", vehicleService.getAllVehicles());
        return "diagnosis/all";
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generateDiagnosisPdf(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        
        return faultDiagnosisService.getDiagnosisById(id)
                .<ResponseEntity<byte[]>>map(diagnosis -> {
                    // Kullanıcı admin değilse ve bu kayıt kullanıcıya ait değilse erişimi engelle
                    if (currentUser != null && 
                        !currentUser.getRole().toString().equals("ROLE_ADMIN") && 
                        !diagnosis.getUser().getId().equals(currentUser.getId())) {
                        return ResponseEntity.status(403).build(); // Forbidden
                    }
                    
                    try {
                        // Set up document with margins (top margin reduced from 90 to 50)
                        Document document = new Document(PageSize.A4, 36, 36, 50, 36);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        PdfWriter writer = PdfWriter.getInstance(document, baos);
                        
                        // Create Unicode font for Turkish characters
                        BaseFont baseFont = BaseFont.createFont("Helvetica", "CP1254", BaseFont.EMBEDDED);
                        Font titleFont = new Font(baseFont, 28, Font.BOLD, new BaseColor(44, 62, 80));
                        Font infoFont = new Font(baseFont, 11, Font.NORMAL, new BaseColor(52, 73, 94));
                        Font infoBoldFont = new Font(baseFont, 11, Font.BOLD, new BaseColor(52, 73, 94));
                        Font tableHeaderFont = new Font(baseFont, 12, Font.BOLD, new BaseColor(44, 62, 80));
                        Font tableCellFont = new Font(baseFont, 11, Font.NORMAL, new BaseColor(52, 73, 94));
                        Font costFont = new Font(baseFont, 14, Font.BOLD, new BaseColor(231, 76, 60));
                        Font footerFont = new Font(baseFont, 8, Font.NORMAL, new BaseColor(127, 140, 141));
                        
                        // Add header and footer
                        writer.setPageEvent(new HeaderFooter(footerFont));
                        
                        document.open();
                        
                        // Add title with custom font and styling (reduced spacing)
                        Paragraph title = new Paragraph("Arıza Teşhis Raporu", titleFont);
                        title.setAlignment(Element.ALIGN_CENTER);
                        title.setSpacingBefore(20);
                        title.setSpacingAfter(15);
                        document.add(title);
                        
                        // Add date and reference number with better styling
                        Paragraph info = new Paragraph();
                        info.setAlignment(Element.ALIGN_RIGHT);
                        info.add(new Chunk("Rapor No: ", infoBoldFont));
                        info.add(new Chunk("#" + diagnosis.getId() + "\n", infoFont));
                        info.add(new Chunk("Tarih: ", infoBoldFont));
                        info.add(new Chunk(diagnosis.getDiagnosisDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), infoFont));
                        info.setSpacingAfter(10);
                        document.add(info);
                        
                        // Add separator line
                        LineSeparator line = new LineSeparator();
                        line.setLineColor(new BaseColor(189, 195, 199));
                        line.setPercentage(100);
                        line.setLineWidth(0.5f);
                        document.add(new Chunk(line));
                        
                        // Vehicle Information Table (reduced spacing)
                        Paragraph vehicleTitle = new Paragraph("Araç Bilgileri", tableHeaderFont);
                        vehicleTitle.setSpacingBefore(15);
                        vehicleTitle.setSpacingAfter(8);
                        document.add(vehicleTitle);
                        
                        PdfPTable vehicleTable = new PdfPTable(2);
                        vehicleTable.setWidthPercentage(100);
                        vehicleTable.setSpacingAfter(20);
                        
                        // Style for table header cells
                        PdfPCell headerCell = new PdfPCell();
                        headerCell.setBackgroundColor(new BaseColor(236, 240, 241));
                        headerCell.setPadding(8);
                        
                        // Style for table content cells
                        PdfPCell contentCell = new PdfPCell();
                        contentCell.setPadding(8);
                        
                        // Add vehicle information
                        addTableRow(vehicleTable, "Plaka:", diagnosis.getVehicle().getPlate(), headerCell, contentCell, tableCellFont);
                        addTableRow(vehicleTable, "Marka/Model:", diagnosis.getVehicle().getBrand() + " " + diagnosis.getVehicle().getModel(), headerCell, contentCell, tableCellFont);
                        addTableRow(vehicleTable, "Kilometre:", diagnosis.getCurrentKilometer() + " km", headerCell, contentCell, tableCellFont);
                        addTableRow(vehicleTable, "Araç Sahibi:", diagnosis.getVehicle().getOwnerName(), headerCell, contentCell, tableCellFont);
                        document.add(vehicleTable);
                        
                        // Diagnosis Details
                        Paragraph diagnosisTitle = new Paragraph("Teşhis Detayları", tableHeaderFont);
                        diagnosisTitle.setSpacingBefore(20);
                        diagnosisTitle.setSpacingAfter(10);
                        document.add(diagnosisTitle);
                        
                        PdfPTable diagnosisTable = new PdfPTable(1);
                        diagnosisTable.setWidthPercentage(100);
                        diagnosisTable.setSpacingAfter(20);
                        
                        // Add diagnosis information with styled cells
                        if (diagnosis.getCustomerComplaint() != null && !diagnosis.getCustomerComplaint().isEmpty()) {
                            PdfPCell complaintHeader = new PdfPCell(new Phrase("Müşteri Şikayeti", tableHeaderFont));
                            complaintHeader.setBackgroundColor(new BaseColor(236, 240, 241));
                            complaintHeader.setPadding(8);
                            diagnosisTable.addCell(complaintHeader);
                            
                            PdfPCell complaintContent = new PdfPCell(new Phrase(diagnosis.getCustomerComplaint(), tableCellFont));
                            complaintContent.setPadding(8);
                            diagnosisTable.addCell(complaintContent);
                        }
                        
                        // Add symptoms with bullet points
                        if (!diagnosis.getSymptoms().isEmpty()) {
                            PdfPCell symptomsHeader = new PdfPCell(new Phrase("Belirtiler", tableHeaderFont));
                            symptomsHeader.setBackgroundColor(new BaseColor(236, 240, 241));
                            symptomsHeader.setPadding(8);
                            diagnosisTable.addCell(symptomsHeader);
                            
                            StringBuilder symptomsText = new StringBuilder();
                            for (String symptom : diagnosis.getSymptoms()) {
                                symptomsText.append("• ").append(symptom).append("\n");
                            }
                            PdfPCell symptomsContent = new PdfPCell(new Phrase(symptomsText.toString(), tableCellFont));
                            symptomsContent.setPadding(8);
                            diagnosisTable.addCell(symptomsContent);
                        }
                        
                        // Add detected faults with bullet points
                        if (!diagnosis.getDetectedFaults().isEmpty()) {
                            PdfPCell faultsHeader = new PdfPCell(new Phrase("Tespit Edilen Arızalar", tableHeaderFont));
                            faultsHeader.setBackgroundColor(new BaseColor(236, 240, 241));
                            faultsHeader.setPadding(8);
                            diagnosisTable.addCell(faultsHeader);
                            
                            StringBuilder faultsText = new StringBuilder();
                            for (String fault : diagnosis.getDetectedFaults()) {
                                faultsText.append("• ").append(fault).append("\n");
                            }
                            PdfPCell faultsContent = new PdfPCell(new Phrase(faultsText.toString(), tableCellFont));
                            faultsContent.setPadding(8);
                            diagnosisTable.addCell(faultsContent);
                        }
                        
                        // Add diagnosis notes
                        if (diagnosis.getDiagnosisNotes() != null && !diagnosis.getDiagnosisNotes().isEmpty()) {
                            PdfPCell notesHeader = new PdfPCell(new Phrase("Teşhis Notları", tableHeaderFont));
                            notesHeader.setBackgroundColor(new BaseColor(236, 240, 241));
                            notesHeader.setPadding(8);
                            diagnosisTable.addCell(notesHeader);
                            
                            PdfPCell notesContent = new PdfPCell(new Phrase(diagnosis.getDiagnosisNotes(), tableCellFont));
                            notesContent.setPadding(8);
                            diagnosisTable.addCell(notesContent);
                        }
                        
                        document.add(diagnosisTable);
                        
                        // Add recommended actions and cost in a separate table
                        PdfPTable summaryTable = new PdfPTable(1);
                        summaryTable.setWidthPercentage(100);
                        summaryTable.setSpacingBefore(20);
                        
                        if (diagnosis.getRecommendedActions() != null && !diagnosis.getRecommendedActions().isEmpty()) {
                            PdfPCell actionsHeader = new PdfPCell(new Phrase("Önerilen İşlemler", tableHeaderFont));
                            actionsHeader.setBackgroundColor(new BaseColor(236, 240, 241));
                            actionsHeader.setPadding(8);
                            summaryTable.addCell(actionsHeader);
                            
                            PdfPCell actionsContent = new PdfPCell(new Phrase(diagnosis.getRecommendedActions(), tableCellFont));
                            actionsContent.setPadding(8);
                            summaryTable.addCell(actionsContent);
                        }
                        
                        // Add estimated cost with special formatting
                        if (diagnosis.getEstimatedCost() != null) {
                            PdfPCell costCell = new PdfPCell();
                            costCell.setPadding(15);
                            costCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            
                            Paragraph costPara = new Paragraph();
                            costPara.add(new Chunk("Tahmini Maliyet: ", tableHeaderFont));
                            costPara.add(new Chunk(String.format("%.2f TL", diagnosis.getEstimatedCost()), costFont));
                            
                            costCell.addElement(costPara);
                            summaryTable.addCell(costCell);
                        }
                        
                        document.add(summaryTable);
                        
                        // Add technician signature section
                        Paragraph signatureSection = new Paragraph();
                        signatureSection.setSpacingBefore(40);
                        signatureSection.setAlignment(Element.ALIGN_RIGHT);
                        signatureSection.add(new Chunk("Teknisyen: ", tableHeaderFont));
                        signatureSection.add(new Chunk(diagnosis.getDiagnosedBy(), tableCellFont));
                        document.add(signatureSection);
                        
                        document.close();
                        
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_PDF);
                        String fileName = String.format("ariza-tespit-%d.pdf", id);
                        headers.setContentDispositionFormData("filename", fileName);
                        
                        return ResponseEntity.ok()
                                .headers(headers)
                                .body(baos.toByteArray());
                                
                    } catch (Exception e) {
                        e.printStackTrace();
                        return ResponseEntity.internalServerError().build();
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    private void addTableRow(PdfPTable table, String header, String content, PdfPCell headerStyle, PdfPCell contentStyle, Font font) {
        PdfPCell headerCell = new PdfPCell(headerStyle);
        headerCell.addElement(new Phrase(header, font));
        table.addCell(headerCell);
        
        PdfPCell contentCell = new PdfPCell(contentStyle);
        contentCell.addElement(new Phrase(content, font));
        table.addCell(contentCell);
    }
    
    // Custom HeaderFooter class for page numbers and footer
    private static class HeaderFooter extends PdfPageEventHelper {
        private final Font footerFont;
        
        public HeaderFooter(Font footerFont) {
            this.footerFont = footerFont;
        }
        
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            
            // Add page number
            String pageNumber = String.format("Sayfa %d", writer.getPageNumber());
            float textBase = document.bottom() - 20;
            
            // Add footer text
            String footerText = "Bu rapor otomatik olarak oluşturulmuştur.";
            
            // Create footer template
            Phrase footer = new Phrase(footerText, footerFont);
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    footer,
                    document.left(), textBase, 0);
            
            // Add page number on the right
            Phrase pageNum = new Phrase(pageNumber, footerFont);
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    pageNum,
                    document.right(), textBase, 0);
        }
    }

    @GetMapping("/{id}/delete")
    public String deleteDiagnosis(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Get the diagnosis to check ownership
        Optional<FaultDiagnosis> diagnosisOpt = faultDiagnosisService.getDiagnosisById(id);
        if (diagnosisOpt.isPresent()) {
            FaultDiagnosis diagnosis = diagnosisOpt.get();
            
            // Check if user is admin or the owner of the diagnosis
            if ("ROLE_ADMIN".equals(currentUser.getRole().toString()) || 
                diagnosis.getUser().getId().equals(currentUser.getId())) {
                faultDiagnosisService.deleteDiagnosis(id);
                redirectAttributes.addFlashAttribute("success", "Arıza teşhisi başarıyla silindi.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Bu arıza teşhisini silme yetkiniz bulunmuyor.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Arıza teşhisi bulunamadı.");
        }
        
        // Redirect back to the vehicle details page
        return "redirect:/vehicles/" + diagnosisOpt.map(d -> d.getVehicle().getId()).orElse(0L);
    }
} 