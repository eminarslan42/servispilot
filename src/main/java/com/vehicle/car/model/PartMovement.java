package com.vehicle.car.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Data
@Table(name = "part_movements")
public class PartMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;

    private String movementType; // GİRİŞ, ÇIKIŞ
    private Integer quantity; // Miktar
    private String reason; // Hareket nedeni (Satın Alma, Servis Kullanımı, vb.)
    private BigDecimal unitPrice; // Birim fiyat
    private String documentNo; // Belge no (Fatura no, İrsaliye no, vb.)
    private String notes; // Notlar

    @ManyToOne
    @JoinColumn(name = "service_record_id")
    private ServiceRecord serviceRecord; // İlişkili servis kaydı (varsa)
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // İşlemi yapan kullanıcı

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 