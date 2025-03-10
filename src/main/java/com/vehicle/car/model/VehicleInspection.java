package com.vehicle.car.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "vehicle_inspections")
public class VehicleInspection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "TEXT")
    private String damageData; // JSON formatında hasar verilerini saklayacak

    private String inspectionType; // Giriş, Çıkış, Periyodik vb.
    private String inspector; // Kontrolü yapan kişi
    
    @Column(columnDefinition = "TEXT")
    private String notes; // Genel notlar

    // Araç detayları
    private Integer currentKilometer;
    private String fuelLevel;
    private Boolean hasSpareWheel;
    private Boolean hasJack;
    private Boolean hasFirstAidKit;
    private Boolean hasWarningTriangle;
    
    // Araç durumu değerlendirmesi
    private Integer exteriorCondition; // 1-5 arası değerlendirme
    private Integer interiorCondition; // 1-5 arası değerlendirme
    private Integer mechanicalCondition; // 1-5 arası değerlendirme
    
    @Column(columnDefinition = "TEXT")
    private String photos; // JSON formatında fotoğraf URL'lerini saklayacak

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 