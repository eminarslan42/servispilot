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
import org.springframework.transaction.annotation.Transactional;
import com.vehicle.car.repository.ServiceReminderRepository;
import com.vehicle.car.repository.ServiceRecordRepository;
import com.vehicle.car.repository.VehicleInspectionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    public Page<Vehicle> getAllVehiclesPaginated(Pageable pageable) {
        return vehicleRepository.findAll(pageable);
    }

    public List<Vehicle> getVehiclesByUserId(Long userId) {
        return vehicleRepository.findByUserId(userId);
    }

    public Page<Vehicle> getVehiclesByUserIdPaginated(Long userId, Pageable pageable) {
        return vehicleRepository.findByUserId(userId, pageable);
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

    public Page<Vehicle> searchVehiclesPaginated(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllVehiclesPaginated(pageable);
        }
        return vehicleRepository.findByPlateContainingIgnoreCaseOrOwnerNameContainingIgnoreCase(
            searchTerm.trim(), searchTerm.trim(), pageable);
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

    public Page<Vehicle> searchVehiclesByUserPaginated(String searchTerm, Long userId, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getVehiclesByUserIdPaginated(userId, pageable);
        }
        return vehicleRepository.findByUserIdAndPlateContainingIgnoreCaseOrUserIdAndOwnerNameContainingIgnoreCase(
            userId, searchTerm.trim(), userId, searchTerm.trim(), pageable);
    }

    public boolean isPlateExists(String plate) {
        return !vehicleRepository.findByPlateContainingIgnoreCase(plate).isEmpty();
    }

    /**
     * Belirli bir plakaya sahip başka araç olup olmadığını kontrol eder, 
     * verilen ID'ye sahip aracı hariç tutar
     */
    public boolean isPlateExistsExcludingVehicle(String plate, Long vehicleId) {
        List<Vehicle> vehicles = vehicleRepository.findByPlateContainingIgnoreCase(plate);
        return vehicles.stream()
                .anyMatch(v -> !v.getId().equals(vehicleId));
    }

    public Vehicle saveVehicle(Vehicle vehicle) {
        // Yeni araç kaydı (ID yoksa)
        if (vehicle.getId() == null) {
            if (isPlateExists(vehicle.getPlate())) {
                throw new IllegalStateException("Bu plakaya sahip bir araç zaten kayıtlıdır");
            }
        } else {
            // Güncelleme işlemi - aynı plakaya sahip farklı bir araç var mı kontrol et
            if (isPlateExistsExcludingVehicle(vehicle.getPlate(), vehicle.getId())) {
                throw new IllegalStateException("Bu plakaya sahip başka bir araç zaten kayıtlıdır");
            }
        }
        return vehicleRepository.save(vehicle);
    }

    public Vehicle saveVehicleForUser(Vehicle vehicle, Long userId) {
        // Yeni araç kaydı (ID yoksa)
        if (vehicle.getId() == null) {
            if (isPlateExists(vehicle.getPlate())) {
                throw new IllegalStateException("Bu plakaya sahip bir araç zaten kayıtlıdır");
            }
        } else {
            // Güncelleme işlemi - aynı plakaya sahip farklı bir araç var mı kontrol et
            if (isPlateExistsExcludingVehicle(vehicle.getPlate(), vehicle.getId())) {
                throw new IllegalStateException("Bu plakaya sahip başka bir araç zaten kayıtlıdır");
            }
        }
        
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