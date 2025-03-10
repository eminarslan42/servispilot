package com.vehicle.car.service;

import com.vehicle.car.model.VehicleInspection;
import com.vehicle.car.repository.VehicleInspectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehicleInspectionService {
    private final VehicleInspectionRepository vehicleInspectionRepository;

    public List<VehicleInspection> getAllInspections() {
        return vehicleInspectionRepository.findAll();
    }

    public Optional<VehicleInspection> getInspectionById(Long id) {
        return vehicleInspectionRepository.findById(id);
    }

    public List<VehicleInspection> getInspectionsByVehicleId(Long vehicleId) {
        return vehicleInspectionRepository.findByVehicleIdOrderByCreatedAtDesc(vehicleId);
    }

    public List<VehicleInspection> getInspectionsByInspector(String inspector) {
        return vehicleInspectionRepository.findByInspectorContainingIgnoreCase(inspector);
    }

    public VehicleInspection saveInspection(VehicleInspection inspection) {
        return vehicleInspectionRepository.save(inspection);
    }

    public void deleteInspection(Long id) {
        vehicleInspectionRepository.deleteById(id);
    }
} 