package com.vehicle.car.service;

import com.vehicle.car.model.ServiceTemplate;
import com.vehicle.car.model.User;
import com.vehicle.car.repository.ServiceTemplateRepository;
import com.vehicle.car.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServiceTemplateService {
    private final ServiceTemplateRepository serviceTemplateRepository;
    private final UserRepository userRepository;

    public List<ServiceTemplate> getAllTemplates() {
        return serviceTemplateRepository.findAll();
    }

    public List<ServiceTemplate> getActiveTemplates() {
        return serviceTemplateRepository.findByIsActiveTrue();
    }
    
    public List<ServiceTemplate> getTemplatesByUserId(Long userId) {
        return serviceTemplateRepository.findByUserId(userId);
    }
    
    public List<ServiceTemplate> getActiveTemplatesByUserId(Long userId) {
        return serviceTemplateRepository.findByUserIdAndIsActiveTrue(userId);
    }

    public Optional<ServiceTemplate> getTemplateById(Long id) {
        return serviceTemplateRepository.findById(id);
    }

    public List<ServiceTemplate> getTemplatesByServiceType(String serviceType) {
        return serviceTemplateRepository.findByServiceType(serviceType);
    }
    
    public List<ServiceTemplate> getTemplatesByServiceTypeAndUserId(String serviceType, Long userId) {
        return serviceTemplateRepository.findByUserIdAndServiceType(userId, serviceType);
    }

    public List<ServiceTemplate> getTemplatesByKilometrage(Integer kilometrage) {
        return serviceTemplateRepository.findByKilometrageIntervalLessThanEqual(kilometrage);
    }
    
    public List<ServiceTemplate> getTemplatesByKilometrageAndUserId(Integer kilometrage, Long userId) {
        return serviceTemplateRepository.findByUserIdAndKilometrageIntervalLessThanEqual(userId, kilometrage);
    }

    public ServiceTemplate saveTemplate(ServiceTemplate template) {
        return serviceTemplateRepository.save(template);
    }
    
    public ServiceTemplate saveTemplateForUser(ServiceTemplate template, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("Kullanıcı bulunamadı"));
        template.setUser(user);
        return serviceTemplateRepository.save(template);
    }

    public void deleteTemplate(Long id) {
        serviceTemplateRepository.deleteById(id);
    }

    public void toggleTemplateStatus(Long id) {
        serviceTemplateRepository.findById(id).ifPresent(template -> {
            template.setIsActive(!template.getIsActive());
            serviceTemplateRepository.save(template);
        });
    }
} 