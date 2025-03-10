package com.vehicle.car.repository;

import com.vehicle.car.model.Vehicle;
import com.vehicle.car.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByPlateContainingIgnoreCase(String plate);
    List<Vehicle> findByOwnerNameContainingIgnoreCase(String ownerName);
    List<Vehicle> findByPlateContainingIgnoreCaseOrOwnerNameContainingIgnoreCase(String plate, String ownerName);
    
    // New methods for user relationship
    List<Vehicle> findByUser(User user);
    List<Vehicle> findByUserId(Long userId);
} 