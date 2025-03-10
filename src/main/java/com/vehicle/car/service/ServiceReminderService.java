package com.vehicle.car.service;

import com.vehicle.car.model.ServiceReminder;
import com.vehicle.car.repository.ServiceReminderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServiceReminderService {
    private final ServiceReminderRepository serviceReminderRepository;

    public List<ServiceReminder> getAllReminders() {
        return serviceReminderRepository.findAll();
    }

    public Optional<ServiceReminder> getReminderById(Long id) {
        return serviceReminderRepository.findById(id);
    }

    public List<ServiceReminder> getRemindersByVehicleId(Long vehicleId) {
        return serviceReminderRepository.findByVehicleId(vehicleId);
    }

    public List<ServiceReminder> getRemindersByPlate(String plate) {
        return serviceReminderRepository.findByVehiclePlate(plate);
    }

    public void saveReminder(ServiceReminder reminder) {
        serviceReminderRepository.save(reminder);
    }

    public void deleteReminder(Long id) {
        serviceReminderRepository.deleteById(id);
    }

    public List<ServiceReminder> getActiveReminders() {
        return serviceReminderRepository.findByStatus("Aktif");
    }
} 