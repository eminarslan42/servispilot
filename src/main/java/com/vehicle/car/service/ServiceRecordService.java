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
} 