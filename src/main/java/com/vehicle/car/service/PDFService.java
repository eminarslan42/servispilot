package com.vehicle.car.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.vehicle.car.model.VehicleInspection;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

@Service
public class PDFService {
    
    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
    
    public byte[] generateInspectionReport(VehicleInspection inspection) {
        try {
            Document document = new Document(PageSize.A4);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, out);
            
            document.open();
            
            // Logo ve Başlık
            addHeader(document, inspection);
            
            // Araç Bilgileri
            addVehicleInfo(document, inspection);
            
            // Kontrol Detayları
            addInspectionDetails(document, inspection);
            
            // Ekipman Durumu
            addEquipmentStatus(document, inspection);
            
            // Durum Değerlendirmeleri
            addConditionRatings(document, inspection);
            
            // Hasar/Çizik Diyagramı
            if (inspection.getDamageData() != null && !inspection.getDamageData().isEmpty()) {
                addDamageDiagram(document, writer, inspection);
            }
            
            // Notlar
            if (inspection.getNotes() != null && !inspection.getNotes().isEmpty()) {
                addNotes(document, inspection);
            }
            
            document.close();
            return out.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("PDF oluşturulurken hata oluştu", e);
        }
    }
    
    private void addHeader(Document document, VehicleInspection inspection) throws DocumentException {
        Paragraph title = new Paragraph("ARAÇ EKSPERTİZ RAPORU", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Paragraph date = new Paragraph("Rapor Tarihi: " + inspection.getCreatedAt().format(formatter), NORMAL_FONT);
        date.setAlignment(Element.ALIGN_RIGHT);
        date.setSpacingAfter(20);
        document.add(date);
    }
    
    private void addVehicleInfo(Document document, VehicleInspection inspection) throws DocumentException {
        Paragraph header = new Paragraph("ARAÇ BİLGİLERİ", HEADER_FONT);
        header.setSpacingAfter(10);
        document.add(header);
        
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(20);
        
        addTableRow(table, "Plaka", inspection.getVehicle().getPlate());
        addTableRow(table, "Marka/Model", inspection.getVehicle().getBrand() + " " + inspection.getVehicle().getModel());
        addTableRow(table, "Kilometre", String.valueOf(inspection.getCurrentKilometer()));
        addTableRow(table, "Yakıt Durumu", getFuelLevelText(inspection.getFuelLevel()));
        
        document.add(table);
    }
    
    private void addInspectionDetails(Document document, VehicleInspection inspection) throws DocumentException {
        Paragraph header = new Paragraph("KONTROL DETAYLARI", HEADER_FONT);
        header.setSpacingAfter(10);
        document.add(header);
        
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(20);
        
        addTableRow(table, "Kontrol Tipi", getInspectionTypeText(inspection.getInspectionType()));
        addTableRow(table, "Kontrol Eden", inspection.getInspector());
        
        document.add(table);
    }
    
    private void addEquipmentStatus(Document document, VehicleInspection inspection) throws DocumentException {
        Paragraph header = new Paragraph("EKİPMAN DURUMU", HEADER_FONT);
        header.setSpacingAfter(10);
        document.add(header);
        
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(20);
        
        addTableRow(table, "Stepne", inspection.getHasSpareWheel() ? "Var" : "Yok");
        addTableRow(table, "Kriko", inspection.getHasJack() ? "Var" : "Yok");
        addTableRow(table, "İlk Yardım Çantası", inspection.getHasFirstAidKit() ? "Var" : "Yok");
        addTableRow(table, "Reflektör", inspection.getHasWarningTriangle() ? "Var" : "Yok");
        
        document.add(table);
    }
    
    private void addConditionRatings(Document document, VehicleInspection inspection) throws DocumentException {
        Paragraph header = new Paragraph("DURUM DEĞERLENDİRMELERİ", HEADER_FONT);
        header.setSpacingAfter(10);
        document.add(header);
        
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(20);
        
        addTableRow(table, "Dış Görünüm", getStarRating(inspection.getExteriorCondition()));
        addTableRow(table, "İç Görünüm", getStarRating(inspection.getInteriorCondition()));
        addTableRow(table, "Mekanik Durum", getStarRating(inspection.getMechanicalCondition()));
        
        document.add(table);
    }
    
    private void addDamageDiagram(Document document, PdfWriter writer, VehicleInspection inspection) throws DocumentException {
        Paragraph header = new Paragraph("HASAR/ÇİZİK DİYAGRAMI", HEADER_FONT);
        header.setSpacingAfter(10);
        document.add(header);
        
        try {
            // Araç diyagramı resmini byte array olarak oku
            byte[] imageBytes = getClass().getClassLoader().getResourceAsStream("static/images/car-diagram.png").readAllBytes();
            Image carImage = Image.getInstance(imageBytes);
            
            // Resim boyutunu ayarla
            float pageWidth = document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();
            float desiredWidth = pageWidth * 0.8f; // Sayfanın %80'i kadar genişlik
            float scale = desiredWidth / carImage.getWidth();
            carImage.scalePercent(scale * 100);
            
            // Resmi sayfanın ortasına yerleştir
            carImage.setAlignment(Element.ALIGN_CENTER);
            document.add(carImage);
            
            // Hasar noktalarını çiz
            if (inspection.getDamageData() != null && !inspection.getDamageData().isEmpty()) {
                List<DamagePoint> damagePoints = parseDamageData(inspection.getDamageData());
                PdfContentByte canvas = writer.getDirectContent();
                
                // Resmin gerçek konumunu hesapla
                float imageX = (document.getPageSize().getWidth() - carImage.getScaledWidth()) / 2;
                float imageY = writer.getVerticalPosition(false);
                
                for (DamagePoint point : damagePoints) {
                    try {
                        // Nokta koordinatlarını hesapla
                        float x = imageX + (carImage.getScaledWidth() * Float.parseFloat(point.x) / 100);
                        float y = imageY - (carImage.getScaledHeight() * (1 - Float.parseFloat(point.y) / 100));
                        
                        // Hasar tipine göre renk belirle
                        BaseColor color = getDamageTypeColor(point.type);
                        canvas.setColorFill(color);
                        canvas.setColorStroke(color);
                        
                        // Hasar şiddetine göre boyut belirle
                        float size = getDamageSize(point.severity);
                        
                        // Daireyi çiz
                        canvas.circle(x, y, size);
                        canvas.fillStroke();
                    } catch (Exception e) {
                        // Tek bir noktanın çiziminde hata olursa diğer noktaları çizmeye devam et
                        continue;
                    }
                }
            }
            
            // Lejant ekle
            document.add(new Paragraph("\n"));
            addDamageLegend(document);
            
        } catch (Exception e) {
            // Hata detayını PDF'e ekle
            document.add(new Paragraph("Araç diyagramı yüklenirken hata oluştu: " + e.getMessage(), NORMAL_FONT));
            e.printStackTrace(); // Konsola hata detayını yazdır
        }
    }
    
    private List<DamagePoint> parseDamageData(String damageData) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(damageData, new TypeReference<List<DamagePoint>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    private BaseColor getDamageTypeColor(String type) {
        switch (type) {
            case "HASAR": return new BaseColor(220, 53, 69); // #dc3545
            case "CIZIK": return new BaseColor(255, 193, 7); // #ffc107
            case "BOYA": return new BaseColor(13, 202, 240);  // #0dcaf0
            default: return BaseColor.GRAY;
        }
    }
    
    private float getDamageSize(String severity) {
        switch (severity) {
            case "HAFIF": return 4f;
            case "ORTA": return 6f;
            case "AGIR": return 8f;
            default: return 6f;
        }
    }
    
    private void addDamageLegend(Document document) throws DocumentException {
        document.add(new Paragraph("\n"));
        PdfPTable legend = new PdfPTable(3);
        legend.setWidthPercentage(100);
        
        // Hasar türleri
        addLegendItem(legend, "HASAR", new BaseColor(220, 53, 69));
        addLegendItem(legend, "ÇİZİK", new BaseColor(255, 193, 7));
        addLegendItem(legend, "BOYA", new BaseColor(13, 202, 240));
        
        document.add(legend);
        
        // Şiddet seviyeleri
        document.add(new Paragraph("\n"));
        PdfPTable severityLegend = new PdfPTable(3);
        severityLegend.setWidthPercentage(100);
        
        Font smallFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        severityLegend.addCell(createLegendCell("HAFİF: Küçük Nokta", smallFont));
        severityLegend.addCell(createLegendCell("ORTA: Orta Nokta", smallFont));
        severityLegend.addCell(createLegendCell("AĞIR: Büyük Nokta", smallFont));
        
        document.add(severityLegend);
    }
    
    private void addLegendItem(PdfPTable table, String text, BaseColor color) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        
        // Renkli daire ve metin içeren hücre oluştur
        Paragraph p = new Paragraph();
        p.add(new Chunk("● ", new Font(Font.FontFamily.ZAPFDINGBATS, 12, Font.NORMAL, color)));
        p.add(new Chunk(text, NORMAL_FONT));
        cell.addElement(p);
        
        table.addCell(cell);
    }
    
    private PdfPCell createLegendCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        return cell;
    }
    
    private static class DamagePoint {
        public String x;
        public String y;
        public String type;
        public String severity;
    }
    
    private void addNotes(Document document, VehicleInspection inspection) throws DocumentException {
        Paragraph header = new Paragraph("NOTLAR", HEADER_FONT);
        header.setSpacingAfter(10);
        document.add(header);
        
        Paragraph notes = new Paragraph(inspection.getNotes(), NORMAL_FONT);
        notes.setSpacingAfter(20);
        document.add(notes);
    }
    
    private void addTableRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, NORMAL_FONT));
        labelCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        labelCell.setPadding(5);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setPadding(5);
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
    
    private String getStarRating(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rating; i++) {
            stars.append("★");
        }
        for (int i = rating; i < 5; i++) {
            stars.append("☆");
        }
        return stars.toString();
    }
    
    private String getFuelLevelText(String fuelLevel) {
        switch (fuelLevel) {
            case "EMPTY": return "Boş";
            case "QUARTER": return "1/4";
            case "HALF": return "1/2";
            case "THREE_QUARTERS": return "3/4";
            case "FULL": return "Dolu";
            default: return "";
        }
    }
    
    private String getInspectionTypeText(String type) {
        switch (type) {
            case "GIRIS": return "Giriş Kontrolü";
            case "CIKIS": return "Çıkış Kontrolü";
            case "PERIYODIK": return "Periyodik Kontrol";
            default: return "";
        }
    }
} 