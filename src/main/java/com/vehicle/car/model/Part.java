package com.vehicle.car.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Data
@Table(name = "parts", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"code", "user_id"})
       })
public class Part {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code; // Parça kodu

    private String name; // Parça adı
    private String brand; // Marka
    private String model; // Model
    private String category; // Kategori
    private String description; // Açıklama
    private Integer quantity; // Stok miktarı
    private Integer minimumQuantity; // Minimum stok miktarı
    private BigDecimal purchasePrice; // Alış fiyatı
    private BigDecimal sellingPrice; // Satış fiyatı
    private String location; // Depo konumu
    private String unit; // Birim (Adet, Litre, vb.)
    private String supplier; // Tedarikçi
    private Boolean isActive; // Aktif/Pasif durumu
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // Parçayı ekleyen/yöneten kullanıcı

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_stock_update")
    private LocalDateTime lastStockUpdate;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastStockUpdate = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (quantity == null) {
            quantity = 0;
        }
        if (minimumQuantity == null) {
            minimumQuantity = 0;
        }
    }
} 