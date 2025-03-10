package com.vehicle.car.service;

import com.vehicle.car.model.Vehicle;
import com.vehicle.car.model.User;
import com.vehicle.car.repository.VehicleRepository;
import com.vehicle.car.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.vehicle.car.model.ServiceRecord;
import org.springframework.transaction.annotation.Transactional;
import com.vehicle.car.repository.ServiceReminderRepository;
import com.vehicle.car.repository.ServiceRecordRepository;
import com.vehicle.car.repository.VehicleInspectionRepository;

@Service
@RequiredArgsConstructor
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final ServiceReminderRepository serviceReminderRepository;
    private final ServiceRecordRepository serviceRecordRepository;
    private final VehicleInspectionRepository vehicleInspectionRepository;

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public List<Vehicle> getVehiclesByUserId(Long userId) {
        return vehicleRepository.findByUserId(userId);
    }

    public Optional<Vehicle> getVehicleById(Long id) {
        return vehicleRepository.findById(id);
    }

    public List<Vehicle> searchVehicles(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllVehicles();
        }
        return vehicleRepository.findByPlateContainingIgnoreCaseOrOwnerNameContainingIgnoreCase(
            searchTerm.trim(), searchTerm.trim());
    }

    public List<Vehicle> searchVehiclesByUser(String searchTerm, Long userId) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getVehiclesByUserId(userId);
        }
        return vehicleRepository.findByPlateContainingIgnoreCaseOrOwnerNameContainingIgnoreCase(
            searchTerm.trim(), searchTerm.trim())
            .stream()
            .filter(vehicle -> vehicle.getUser() != null && vehicle.getUser().getId().equals(userId))
            .collect(Collectors.toList());
    }

    public Vehicle saveVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    public Vehicle saveVehicleForUser(Vehicle vehicle, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("Kullanıcı bulunamadı"));
        vehicle.setUser(user);
        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public void deleteVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("Araç bulunamadı"));

        // Aktif servis kayıtları kontrolü
        if (vehicle.getServiceRecords() != null && vehicle.getServiceRecords().stream()
                .anyMatch(service -> "Devam Ediyor".equals(service.getStatus()))) {
            throw new IllegalStateException("Bu araç için devam eden servis kayıtları bulunduğundan silinemez");
        }

        // Servis geçmişi kontrolü
        if (vehicle.getServiceRecords() != null && !vehicle.getServiceRecords().isEmpty()) {
            throw new IllegalStateException("Bu araç için servis geçmişi bulunduğundan silinemez. Önce servis kayıtlarını silmelisiniz.");
        }

        // Servis hatırlatıcıları kontrolü
        if (vehicle.getServiceReminders() != null && !vehicle.getServiceReminders().isEmpty()) {
            throw new IllegalStateException("Bu araç için servis hatırlatıcıları bulunduğundan silinemez. Önce hatırlatıcıları silmelisiniz.");
        }

        // Ekspertiz kayıtları kontrolü
        if (vehicle.getInspections() != null && !vehicle.getInspections().isEmpty()) {
            throw new IllegalStateException("Bu araç için ekspertiz kayıtları bulunduğundan silinemez. Önce ekspertiz kayıtlarını silmelisiniz.");
        }

        try {
            // İlişkili kayıtları sil
            serviceReminderRepository.deleteByVehicleId(id);
            serviceRecordRepository.deleteByVehicleId(id);
            vehicleInspectionRepository.deleteByVehicleId(id);
            
            // Aracı sil
            vehicleRepository.delete(vehicle);
            vehicleRepository.flush();
        } catch (Exception e) {
            throw new IllegalStateException("Araç silinirken bir hata oluştu: " + e.getMessage());
        }
    }
} 