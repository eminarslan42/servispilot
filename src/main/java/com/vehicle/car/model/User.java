package com.vehicle.car.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        })
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 20)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @NotBlank
    @Size(max = 120)
    private String password;
    
    @Size(max = 50)
    private String firstName;
    
    @Size(max = 50)
    private String lastName;
    
    @Size(max = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Subscription subscription;
    
    // New relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Vehicle> vehicles;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ServiceRecord> serviceRecords;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ServiceReminder> serviceReminders;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Account> accounts;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Transaction> transactions;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<FaultDiagnosis> faultDiagnoses;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<VehicleInspection> vehicleInspections;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ServiceTemplate> serviceTemplates;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Part> parts;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<PartMovement> partMovements;

    public boolean hasActiveSubscription() {
        return subscription != null && 
               subscription.getIsActive() && 
               subscription.getEndDate().isAfter(LocalDateTime.now());
    }

    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", username='" + username + '\'' +
               ", email='" + email + '\'' +
               '}';
    }
} 