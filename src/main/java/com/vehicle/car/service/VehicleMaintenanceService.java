package com.vehicle.car.service;

import com.vehicle.car.model.VehicleMaintenanceHistory;
import com.vehicle.car.model.Vehicle;
import com.vehicle.car.repository.VehicleMaintenanceHistoryRepository;
import com.vehicle.car.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehicleMaintenanceService {
    
    private final VehicleMaintenanceHistoryRepository maintenanceRepository;
    private final VehicleRepository vehicleRepository;
    
    // Tüm bakım kayıtlarını getir
    public List<VehicleMaintenanceHistory> getAllMaintenanceRecords() {
        return maintenanceRepository.findAll();
    }
    
    // Yeni bakım kaydı ekle
    public VehicleMaintenanceHistory addMaintenanceRecord(VehicleMaintenanceHistory maintenance) {
        // Aracın mevcut kilometresini güncelle
        Vehicle vehicle = maintenance.getVehicle();
        vehicle.setCurrentKilometer(maintenance.getKilometerage());
        vehicleRepository.save(vehicle);
        
        return maintenanceRepository.save(maintenance);
    }
    
    // Bakım kaydını güncelle
    public VehicleMaintenanceHistory updateMaintenanceRecord(Long id, VehicleMaintenanceHistory maintenance) {
        if (maintenanceRepository.existsById(id)) {
            maintenance.setId(id);
            return maintenanceRepository.save(maintenance);
        }
        throw new RuntimeException("Bakım kaydı bulunamadı: " + id);
    }
    
    // Bakım kaydını sil
    public void deleteMaintenanceRecord(Long id) {
        maintenanceRepository.deleteById(id);
    }
    
    // Bakım kaydını getir
    public Optional<VehicleMaintenanceHistory> getMaintenanceRecord(Long id) {
        return maintenanceRepository.findById(id);
    }
    
    // Aracın tüm bakım geçmişini getir
    public List<VehicleMaintenanceHistory> getVehicleMaintenanceHistory(Long vehicleId) {
        return maintenanceRepository.findByVehicleIdOrderByCreatedAtDesc(vehicleId);
    }
    
    // Yaklaşan bakımları getir
    public List<VehicleMaintenanceHistory> getUpcomingMaintenance() {
        LocalDateTime nextMonth = LocalDateTime.now().plusMonths(1);
        return maintenanceRepository.findUpcomingMaintenance(nextMonth, null);
    }
    
    // Garanti kapsamındaki işlemleri getir
    public List<VehicleMaintenanceHistory> getWarrantyServices(Long vehicleId) {
        return maintenanceRepository.findByVehicleIdAndIsWarrantyWorkTrueAndWarrantyEndDateAfter(
            vehicleId, LocalDateTime.now());
    }
    
    // Bakım tipine göre geçmişi getir
    public List<VehicleMaintenanceHistory> getMaintenanceHistoryByType(Long vehicleId, String maintenanceType) {
        return maintenanceRepository.findByVehicleIdAndMaintenanceTypeOrderByCreatedAtDesc(
            vehicleId, maintenanceType);
    }
    
    // Teknisyene göre işlemleri getir
    public List<VehicleMaintenanceHistory> getMaintenanceHistoryByTechnician(String technician) {
        return maintenanceRepository.findByTechnicianOrderByCreatedAtDesc(technician);
    }
    
    // Belirli bir tarih aralığındaki bakımları getir
    public List<VehicleMaintenanceHistory> getMaintenanceHistoryByDateRange(
            Long vehicleId, LocalDateTime startDate, LocalDateTime endDate) {
        return maintenanceRepository.findByVehicleIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            vehicleId, startDate, endDate);
    }
    
    // Kilometre bazlı bakım kontrolü
    public boolean isMaintenanceNeeded(Vehicle vehicle) {
        List<VehicleMaintenanceHistory> history = getVehicleMaintenanceHistory(vehicle.getId());
        if (!history.isEmpty()) {
            VehicleMaintenanceHistory lastMaintenance = history.get(0);
            if (lastMaintenance.getNextMaintenanceKm() != null) {
                return vehicle.getCurrentKilometer() >= lastMaintenance.getNextMaintenanceKm();
            }
        }
        return false;
    }
    
    // Bakım maliyeti analizi
    public double calculateTotalMaintenanceCost(Long vehicleId, LocalDateTime startDate, LocalDateTime endDate) {
        List<VehicleMaintenanceHistory> records = getMaintenanceHistoryByDateRange(vehicleId, startDate, endDate);
        return records.stream()
            .mapToDouble(VehicleMaintenanceHistory::getCost)
            .sum();
    }
} 