package com.vehicle.car.repository;

import com.vehicle.car.model.VehicleInspection;
import com.vehicle.car.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VehicleInspectionRepository extends JpaRepository<VehicleInspection, Long> {
    List<VehicleInspection> findByVehicleId(Long vehicleId);
    List<VehicleInspection> findByVehicleIdOrderByCreatedAtDesc(Long vehicleId);
    List<VehicleInspection> findByInspectorContainingIgnoreCase(String inspector);
    void deleteByVehicleId(Long vehicleId);
    
    // New methods for user relationship
    List<VehicleInspection> findByUser(User user);
    List<VehicleInspection> findByUserId(Long userId);
    void deleteByUserId(Long userId);
} 