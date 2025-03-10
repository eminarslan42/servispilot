package com.vehicle.car.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class VehicleMakeModelService {
    private final Map<String, List<String>> popularMakesAndModels;

    public VehicleMakeModelService() {
        popularMakesAndModels = new LinkedHashMap<>(); // LinkedHashMap to maintain insertion order
        
        // Volkswagen
        popularMakesAndModels.put("Volkswagen", Arrays.asList(
            "Passat", "Golf", "Polo", "Tiguan", "T-Roc", "Arteon"
        ));

        // Toyota
        popularMakesAndModels.put("Toyota", Arrays.asList(
            "Corolla", "Yaris", "C-HR", "RAV4", "Camry"
        ));

        // Renault
        popularMakesAndModels.put("Renault", Arrays.asList(
            "Clio", "Megane", "Captur", "Kadjar", "Talisman"
        ));

        // Ford
        popularMakesAndModels.put("Ford", Arrays.asList(
            "Focus", "Fiesta", "Kuga", "Puma", "EcoSport"
        ));

        // Fiat
        popularMakesAndModels.put("Fiat", Arrays.asList(
            "Egea", "Panda", "500", "Doblo", "Tipo"
        ));

        // Honda
        popularMakesAndModels.put("Honda", Arrays.asList(
            "Civic", "CR-V", "Jazz", "HR-V", "City"
        ));

        // Hyundai
        popularMakesAndModels.put("Hyundai", Arrays.asList(
            "i20", "i10", "Tucson", "Elantra", "Kona"
        ));

        // Mercedes-Benz
        popularMakesAndModels.put("Mercedes-Benz", Arrays.asList(
            "A-Serisi", "C-Serisi", "E-Serisi", "GLA", "CLA"
        ));

        // BMW
        popularMakesAndModels.put("BMW", Arrays.asList(
            "3 Serisi", "5 Serisi", "X1", "X3", "1 Serisi"
        ));

        // Audi
        popularMakesAndModels.put("Audi", Arrays.asList(
            "A3", "A4", "A6", "Q2", "Q3"
        ));

        // Peugeot
        popularMakesAndModels.put("Peugeot", Arrays.asList(
            "208", "2008", "301", "308", "3008"
        ));

        // Opel
        popularMakesAndModels.put("Opel", Arrays.asList(
            "Corsa", "Astra", "Mokka", "Crossland", "Grandland"
        ));
    }

    public List<String> getAllMakes() {
        return new ArrayList<>(popularMakesAndModels.keySet());
    }

    public List<String> getModelsByMake(String make) {
        return popularMakesAndModels.getOrDefault(make, new ArrayList<>());
    }
} 