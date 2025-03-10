package com.vehicle.car.repository;

import com.vehicle.car.model.ServiceRecord;
import com.vehicle.car.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Long> {
    List<ServiceRecord> findByVehicleId(Long vehicleId);
    List<ServiceRecord> findByVehiclePlate(String plate);
    void deleteByVehicleId(Long vehicleId);
    
    // New methods for user relationship
    List<ServiceRecord> findByUser(User user);
    List<ServiceRecord> findByUserId(Long userId);
    void deleteByUserId(Long userId);
} 