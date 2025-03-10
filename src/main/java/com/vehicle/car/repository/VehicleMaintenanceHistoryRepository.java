package com.vehicle.car.repository;

import com.vehicle.car.model.VehicleMaintenanceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VehicleMaintenanceHistoryRepository extends JpaRepository<VehicleMaintenanceHistory, Long> {
    
    // Araca göre bakım geçmişini getir
    List<VehicleMaintenanceHistory> findByVehicleIdOrderByCreatedAtDesc(Long vehicleId);
    
    // Yaklaşan bakımları getir
    @Query("SELECT vh FROM VehicleMaintenanceHistory vh WHERE vh.nextMaintenanceDate <= :date OR vh.nextMaintenanceKm <= :currentKm")
    List<VehicleMaintenanceHistory> findUpcomingMaintenance(@Param("date") LocalDateTime date, @Param("currentKm") Integer currentKm);
    
    // Garanti kapsamındaki işlemleri getir
    List<VehicleMaintenanceHistory> findByVehicleIdAndIsWarrantyWorkTrueAndWarrantyEndDateAfter(Long vehicleId, LocalDateTime now);
    
    // Bakım tipine göre geçmişi getir
    List<VehicleMaintenanceHistory> findByVehicleIdAndMaintenanceTypeOrderByCreatedAtDesc(Long vehicleId, String maintenanceType);
    
    // Teknisyene göre işlemleri getir
    List<VehicleMaintenanceHistory> findByTechnicianOrderByCreatedAtDesc(String technician);
    
    // Belirli bir tarih aralığındaki bakımları getir
    List<VehicleMaintenanceHistory> findByVehicleIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        Long vehicleId, LocalDateTime startDate, LocalDateTime endDate);
} 