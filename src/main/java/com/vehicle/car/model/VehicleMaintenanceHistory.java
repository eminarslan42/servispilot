package com.vehicle.car.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "vehicle_maintenance_history")
public class VehicleMaintenanceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    private String maintenanceType; // PERIYODIK, MOTOR, FREN, ELEKTRIK, vb.
    
    private Integer kilometerage; // İşlem yapıldığındaki kilometre
    
    private Integer nextMaintenanceKm; // Bir sonraki bakım kilometresi
    
    private LocalDateTime nextMaintenanceDate; // Bir sonraki bakım tarihi
    
    private String description; // Yapılan işlem açıklaması
    
    private String partsUsed; // Kullanılan parçalar
    
    private Double cost; // İşlem maliyeti
    
    private String technician; // İşlemi yapan teknisyen
    
    private String recommendations; // Öneriler ve notlar
    
    private Boolean isWarrantyWork; // Garanti kapsamında mı?
    
    private LocalDateTime warrantyEndDate; // Garanti bitiş tarihi
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 