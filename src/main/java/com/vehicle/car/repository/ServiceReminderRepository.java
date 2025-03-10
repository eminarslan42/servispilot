package com.vehicle.car.repository;

import com.vehicle.car.model.ServiceReminder;
import com.vehicle.car.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceReminderRepository extends JpaRepository<ServiceReminder, Long> {
    List<ServiceReminder> findByVehicleId(Long vehicleId);
    List<ServiceReminder> findByStatus(String status);
    List<ServiceReminder> findByNextServiceDateBeforeAndStatusAndNotificationSentFalse(
        LocalDateTime date, String status);
    List<ServiceReminder> findByVehiclePlate(String plate);
    void deleteByVehicleId(Long vehicleId);
    
    // New methods for user relationship
    List<ServiceReminder> findByUser(User user);
    List<ServiceReminder> findByUserId(Long userId);
    void deleteByUserId(Long userId);
} 