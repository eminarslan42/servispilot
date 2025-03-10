package com.vehicle.car.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Data
@Table(name = "service_records")
public class ServiceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime serviceDate; // Servis Tarihi
    private String description; // Yapılan İşlem Açıklaması
    private String technician; // Teknisyen/Usta
    private BigDecimal cost; // Maliyet
    private String status; // Durum (Devam Ediyor, Tamamlandı, vb.)
    private Integer kilometerage; // Kilometre
    private String parts; // Kullanılan Parçalar
    private String notes; // Ek Notlar

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 