package com.vehicle.car.service;

import com.vehicle.car.model.ServiceReminder;
import com.vehicle.car.repository.ServiceReminderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

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

    public List<ServiceReminder> getActiveRemindersByUserId(Long userId) {
        return serviceReminderRepository.findByUserId(userId).stream()
                .filter(reminder -> "Aktif".equals(reminder.getStatus()))
                .toList();
    }

    /**
     * Yaklaşan hatırlatıcıları getirir (bugünden itibaren 1 gün içinde olan)
     */
    public List<ServiceReminder> getUpcomingReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        
        return serviceReminderRepository.findByStatus("Aktif").stream()
                .filter(reminder -> {
                    LocalDateTime reminderDate = reminder.getReminderDate();
                    return reminderDate != null && 
                           reminderDate.isAfter(now) && 
                           reminderDate.isBefore(tomorrow);
                })
                .toList();
    }
    
    /**
     * Kullanıcı ID'sine göre yaklaşan hatırlatıcıları getirir (bugünden itibaren 1 gün içinde olan)
     */
    public List<ServiceReminder> getUpcomingRemindersByUserId(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        
        return serviceReminderRepository.findByUserId(userId).stream()
                .filter(reminder -> "Aktif".equals(reminder.getStatus()))
                .filter(reminder -> {
                    LocalDateTime reminderDate = reminder.getReminderDate();
                    return reminderDate != null && 
                           reminderDate.isAfter(now) && 
                           reminderDate.isBefore(tomorrow);
                })
                .toList();
    }
} 