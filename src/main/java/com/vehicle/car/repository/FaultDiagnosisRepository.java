package com.vehicle.car.repository;

import com.vehicle.car.model.FaultDiagnosis;
import com.vehicle.car.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FaultDiagnosisRepository extends JpaRepository<FaultDiagnosis, Long> {
    List<FaultDiagnosis> findByVehicleIdOrderByDiagnosisDateDesc(Long vehicleId);
    List<FaultDiagnosis> findByVehicleId(Long vehicleId);
    List<FaultDiagnosis> findByStatus(String status);
    List<FaultDiagnosis> findByDiagnosedBy(String technician);
    
    // New methods for user relationship
    List<FaultDiagnosis> findByUser(User user);
    List<FaultDiagnosis> findByUserId(Long userId);
    void deleteByUserId(Long userId);
} 