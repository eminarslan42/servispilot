package com.vehicle.car.service;

import com.vehicle.car.model.FaultDiagnosis;
import com.vehicle.car.model.User;
import com.vehicle.car.repository.FaultDiagnosisRepository;
import com.vehicle.car.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FaultDiagnosisService {
    private final FaultDiagnosisRepository faultDiagnosisRepository;
    private final UserRepository userRepository;

    public List<FaultDiagnosis> getAllDiagnoses() {
        return faultDiagnosisRepository.findAll();
    }

    public List<FaultDiagnosis> getDiagnosesByVehicleId(Long vehicleId) {
        return faultDiagnosisRepository.findByVehicleIdOrderByDiagnosisDateDesc(vehicleId);
    }
    
    public List<FaultDiagnosis> getDiagnosesByUserId(Long userId) {
        return faultDiagnosisRepository.findByUserId(userId);
    }
    
    public List<FaultDiagnosis> getDiagnosesByUsername(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            return faultDiagnosisRepository.findByUserId(user.getId());
        }
        
        // Kullanıcı yoksa, diagnosedBy alanı eşleşenleri bul
        return faultDiagnosisRepository.findByDiagnosedBy(username);
    }

    public Optional<FaultDiagnosis> getDiagnosisById(Long id) {
        return faultDiagnosisRepository.findById(id);
    }

    public FaultDiagnosis saveDiagnosis(FaultDiagnosis diagnosis) {
        if (diagnosis.getDiagnosisDate() == null) {
            diagnosis.setDiagnosisDate(LocalDateTime.now());
        }
        if (diagnosis.getStatus() == null) {
            diagnosis.setStatus("Yeni");
        }
        
        // Eğer user ilişkisi yoksa ama diagnosedBy alanı doluysa, bu kullanıcı adıyla eşleşen bir kullanıcı bul
        if (diagnosis.getUser() == null && diagnosis.getDiagnosedBy() != null && !diagnosis.getDiagnosedBy().isEmpty()) {
            userRepository.findByUsername(diagnosis.getDiagnosedBy())
                .ifPresent(diagnosis::setUser);
        }
        
        // Tam tersi, user ilişkisi varsa ama diagnosedBy alanı boşsa, kullanıcı adını ata
        if (diagnosis.getUser() != null && (diagnosis.getDiagnosedBy() == null || diagnosis.getDiagnosedBy().isEmpty())) {
            diagnosis.setDiagnosedBy(diagnosis.getUser().getUsername());
        }
        
        return faultDiagnosisRepository.save(diagnosis);
    }

    public void deleteDiagnosis(Long id) {
        faultDiagnosisRepository.deleteById(id);
    }

    public List<FaultDiagnosis> getDiagnosesByStatus(String status) {
        return faultDiagnosisRepository.findByStatus(status);
    }

    public List<FaultDiagnosis> getDiagnosesByTechnician(String technician) {
        return faultDiagnosisRepository.findByDiagnosedBy(technician);
    }
} 