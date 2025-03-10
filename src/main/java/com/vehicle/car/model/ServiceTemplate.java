package com.vehicle.car.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "service_templates")
public class ServiceTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Şablon adı (örn: "10.000 KM Bakımı")
    private String description; // Şablon açıklaması
    private String serviceType; // Servis tipi (Periyodik Bakım, Yağ Değişimi, vb.)
    private Integer kilometrageInterval; // Kilometre aralığı (örn: 10000)
    private Integer monthInterval; // Ay aralığı (örn: 6)
    
    @Column(columnDefinition = "TEXT")
    private String checkList; // Kontrol listesi (JSON formatında)
    
    @Column(columnDefinition = "TEXT")
    private String requiredParts; // Gerekli parçalar (JSON formatında)
    
    private Integer estimatedDuration; // Tahmini süre (dakika)
    private Double estimatedCost; // Tahmini maliyet
    private Boolean isActive; // Aktif/Pasif durumu
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // Şablonu oluşturan kullanıcı

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
    }
} 