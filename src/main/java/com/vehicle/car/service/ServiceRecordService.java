package com.vehicle.car.service;

import com.vehicle.car.model.ServiceRecord;
import com.vehicle.car.model.User;
import com.vehicle.car.repository.ServiceRecordRepository;
import com.vehicle.car.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServiceRecordService {
    private final ServiceRecordRepository serviceRecordRepository;
    private final UserRepository userRepository;

    public List<ServiceRecord> getAllServiceRecords() {
        return serviceRecordRepository.findAll();
    }

    public Optional<ServiceRecord> getServiceRecordById(Long id) {
        return serviceRecordRepository.findById(id);
    }

    public List<ServiceRecord> getServiceRecordsByVehicleId(Long vehicleId) {
        return serviceRecordRepository.findByVehicleId(vehicleId);
    }

    public List<ServiceRecord> getLatestServiceRecordsByVehicleId(Long vehicleId) {
        List<ServiceRecord> records = serviceRecordRepository.findByVehicleId(vehicleId);
        // Servis kayıtlarını en yeni tarihe göre sırala (null tarihleri en sona koy)
        records.sort((a, b) -> {
            if (a.getServiceDate() == null) return 1;
            if (b.getServiceDate() == null) return -1;
            return b.getServiceDate().compareTo(a.getServiceDate());
        });
        return records;
    }

    public List<ServiceRecord> getServiceRecordsByPlate(String plate) {
        return serviceRecordRepository.findByVehiclePlate(plate);
    }
    
    public List<ServiceRecord> getServiceRecordsByUserId(Long userId) {
        return serviceRecordRepository.findByUserId(userId);
    }

    public ServiceRecord saveServiceRecord(ServiceRecord serviceRecord) {
        return serviceRecordRepository.save(serviceRecord);
    }
    
    public ServiceRecord saveServiceRecordForUser(ServiceRecord serviceRecord, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("Kullanıcı bulunamadı"));
        serviceRecord.setUser(user);
        return serviceRecordRepository.save(serviceRecord);
    }

    public void deleteServiceRecord(Long id) {
        serviceRecordRepository.deleteById(id);
    }

    public Optional<ServiceRecord> getLastServiceRecordByVehicleId(Long vehicleId) {
        List<ServiceRecord> records = getLatestServiceRecordsByVehicleId(vehicleId);
        if (records.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(records.get(0));
    }
} 