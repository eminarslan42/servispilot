package com.vehicle.car.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "service_reminders")
public class ServiceReminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String title; // Hatırlatma başlığı
    private String description; // Hatırlatma açıklaması
    private LocalDateTime reminderDate; // Hatırlatma tarihi
    private LocalDateTime nextServiceDate; // Bir sonraki servis tarihi
    private Integer nextServiceKilometer; // Bir sonraki servis kilometresi
    private String status; // Durum (Aktif, Tamamlandı, İptal)
    private String reminderType; // Hatırlatma tipi (Periyodik Bakım, Yağ Değişimi, vb.)
    private Boolean notificationSent; // Bildirim gönderildi mi?

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (notificationSent == null) {
            notificationSent = false;
        }
    }
} 