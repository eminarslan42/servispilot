package com.vehicle.car.controller;

import com.vehicle.car.service.VehicleMakeModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vehicles")
public class VehicleMakeModelController {
    private final VehicleMakeModelService vehicleMakeModelService;

    @GetMapping("/makes")
    public List<String> getAllMakes() {
        return vehicleMakeModelService.getAllMakes();
    }

    @GetMapping("/models/{make}")
    public List<String> getModelsByMake(@PathVariable String make) {
        return vehicleMakeModelService.getModelsByMake(make);
    }
} 