package com.vehicle.car.repository;

import com.vehicle.car.model.Vehicle;
import com.vehicle.car.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByPlateContainingIgnoreCase(String plate);
    List<Vehicle> findByOwnerNameContainingIgnoreCase(String ownerName);
    List<Vehicle> findByPlateContainingIgnoreCaseOrOwnerNameContainingIgnoreCase(String plate, String ownerName);
    Page<Vehicle> findByPlateContainingIgnoreCaseOrOwnerNameContainingIgnoreCase(String plate, String ownerName, Pageable pageable);
    
    // New methods for user relationship
    List<Vehicle> findByUser(User user);
    List<Vehicle> findByUserId(Long userId);
    Page<Vehicle> findByUserId(Long userId, Pageable pageable);
    
    // Paginated search methods for user vehicles
    Page<Vehicle> findByUserIdAndPlateContainingIgnoreCaseOrUserIdAndOwnerNameContainingIgnoreCase(
        Long userId, String plate, Long userId2, String ownerName, Pageable pageable);
}