package com.vehicle.car.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads/vehicle-documents}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            // Proje kök dizini
            String rootPath = new File("").getAbsolutePath();
            System.out.println("Proje kök dizini: " + rootPath);
            
            // Dizini oluştur (eğer yoksa)
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                System.out.println("Dosya yükleme dizini oluşturuldu: " + created);
            }
            
            // Mutlak yolu al
            String uploadPath = directory.getAbsolutePath();
            
            // Dosya yolunu Windows ve Unix sistemleri için uyumlu hale getir
            uploadPath = uploadPath.replace("\\", "/");
            
            // Eğer yol / ile bitmiyorsa ekle
            if (!uploadPath.endsWith("/")) {
                uploadPath += "/";
            }
            
            System.out.println("Dosya yükleme dizini (mutlak yol): " + uploadPath);
            
            // Resource locations için file: protokolünü ekle
            String resourceLocation = "file:" + uploadPath;
            
            // Uploads ana dizini için resource handler ekle
            int vehicleDocumentsIndex = uploadPath.indexOf("vehicle-documents/");
            if (vehicleDocumentsIndex >= 0) {
                String uploadsBaseDir = uploadPath.substring(0, vehicleDocumentsIndex + "vehicle-documents/".length());
                String uploadsResourceLocation = "file:" + uploadsBaseDir;
                
                System.out.println("Uploads base directory: " + uploadsBaseDir);
                System.out.println("Uploads resource location: " + uploadsResourceLocation);
                
                // Kaynak işleyicilerini ekle (daha spesifik olanı önce)
                registry.addResourceHandler("/uploads/vehicle-documents/**")
                        .addResourceLocations(resourceLocation)
                        .setCachePeriod(0); // Önbelleğe almayı devre dışı bırak (geliştirme için)
                
                registry.addResourceHandler("/uploads/**")
                        .addResourceLocations(uploadsResourceLocation)
                        .setCachePeriod(0); // Önbelleğe almayı devre dışı bırak (geliştirme için)
            } else {
                // Fallback - dizin yolu vehicle-documents/ içermiyorsa
                registry.addResourceHandler("/uploads/**")
                        .addResourceLocations(resourceLocation)
                        .setCachePeriod(0);
            }
            
            // Statik dosyalar için varsayılan işleyiciler
            registry.addResourceHandler("/static/**")
                    .addResourceLocations("classpath:/static/")
                    .setCachePeriod(3600); // 1 saat önbellek
                    
            registry.addResourceHandler("/images/**")
                    .addResourceLocations("classpath:/static/images/")
                    .setCachePeriod(3600); // 1 saat önbellek
                    
            registry.addResourceHandler("/js/**")
                    .addResourceLocations("classpath:/static/js/")
                    .setCachePeriod(3600); // 1 saat önbellek
                    
            registry.addResourceHandler("/css/**")
                    .addResourceLocations("classpath:/static/css/")
                    .setCachePeriod(3600); // 1 saat önbellek
            
        } catch (Exception e) {
            System.err.println("Dosya yükleme dizini yapılandırılırken hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 