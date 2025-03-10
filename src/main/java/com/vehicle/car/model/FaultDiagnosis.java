package com.vehicle.car.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "fault_diagnoses")
public class FaultDiagnosis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private LocalDateTime diagnosisDate;

    private String diagnosedBy; // Teknisyen
    private Integer currentKilometer;
    
    @Column(columnDefinition = "TEXT")
    private String customerComplaint;
    
    @Column(columnDefinition = "TEXT")
    private String diagnosisNotes;
    
    @ElementCollection
    @CollectionTable(name = "fault_symptoms", joinColumns = @JoinColumn(name = "diagnosis_id"))
    @Column(name = "symptom")
    private List<String> symptoms = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "detected_faults", joinColumns = @JoinColumn(name = "diagnosis_id"))
    @Column(name = "fault")
    private List<String> detectedFaults = new ArrayList<>();
    
    @Column(columnDefinition = "TEXT")
    private String recommendedActions;
    
    private String status;
    
    private Double estimatedCost;

    @PrePersist
    protected void onCreate() {
        diagnosisDate = LocalDateTime.now();
        if (status == null) {
            status = "Yeni";
        }
        if (symptoms == null) {
            symptoms = new ArrayList<>();
        }
        if (detectedFaults == null) {
            detectedFaults = new ArrayList<>();
        }
    }
} 