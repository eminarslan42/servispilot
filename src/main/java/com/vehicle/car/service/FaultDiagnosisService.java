package com.vehicle.car.service;

import com.vehicle.car.model.FaultDiagnosis;
import com.vehicle.car.repository.FaultDiagnosisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FaultDiagnosisService {
    private final FaultDiagnosisRepository faultDiagnosisRepository;

    public List<FaultDiagnosis> getAllDiagnoses() {
        return faultDiagnosisRepository.findAll();
    }

    public List<FaultDiagnosis> getDiagnosesByVehicleId(Long vehicleId) {
        return faultDiagnosisRepository.findByVehicleIdOrderByDiagnosisDateDesc(vehicleId);
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