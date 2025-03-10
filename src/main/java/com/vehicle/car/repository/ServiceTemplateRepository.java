package com.vehicle.car.repository;

import com.vehicle.car.model.ServiceTemplate;
import com.vehicle.car.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServiceTemplateRepository extends JpaRepository<ServiceTemplate, Long> {
    List<ServiceTemplate> findByIsActiveTrue();
    List<ServiceTemplate> findByServiceType(String serviceType);
    List<ServiceTemplate> findByKilometrageIntervalLessThanEqual(Integer kilometrage);
    
    // User ilişkisi için yeni metodlar
    List<ServiceTemplate> findByUser(User user);
    List<ServiceTemplate> findByUserId(Long userId);
    List<ServiceTemplate> findByUserIdAndIsActiveTrue(Long userId);
    List<ServiceTemplate> findByUserIdAndServiceType(Long userId, String serviceType);
    List<ServiceTemplate> findByUserIdAndKilometrageIntervalLessThanEqual(Long userId, Integer kilometrage);
} 