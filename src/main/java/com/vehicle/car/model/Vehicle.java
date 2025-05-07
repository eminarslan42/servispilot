package com.vehicle.car.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "vehicles")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String plate; // Plaka
    private String brand; // Marka
    private String model; // Model
    private Integer year; // Yıl
    private String ownerName; // Araç Sahibinin Adı
    private String ownerPhone; // Araç Sahibinin Telefonu
    private Integer currentKilometer; // Güncel Kilometre

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "vehicle")
    private List<ServiceRecord> serviceRecords;

    @OneToMany(mappedBy = "vehicle")
    private List<ServiceReminder> serviceReminders;

    @OneToMany(mappedBy = "vehicle")
    private List<VehicleInspection> inspections;

    @OneToMany(mappedBy = "vehicle")
    private List<FaultDiagnosis> faultDiagnoses;
    
    
} 