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
            "Passat", "Golf","Caddy","Caravelle","Transporter", "Polo","Bora","Jetta","Tiguan", "T-Roc","T-Cross","Taigo", "Arteon","Diğer"
        ));

        // Toyota
        popularMakesAndModels.put("Toyota", Arrays.asList(
            "Corolla","Auris", "Yaris", "C-HR", "RAV4", "Camry","Diğer"
        ));

        // Renault
        popularMakesAndModels.put("Renault", Arrays.asList(
            "Clio","Fluence","Laguna","Kangoo","Master","Trafic","Express","Symbol","Taliant","Broadway","Toros","Europa","Duster", "Megane", "Captur", "Kadjar", "Talisman","Diğer"
        ));

        // Ford
        popularMakesAndModels.put("Ford", Arrays.asList(
            "Focus","C-Max","Escort","Fusion","Mondeo","Ranger","Fiesta", "Kuga", "Puma", "EcoSport","Diğer"
        ));

        // Fiat
        popularMakesAndModels.put("Fiat", Arrays.asList(
            "Egea","Egea Cross","Albea","Linea","Marea","Palio","Punto","Siena","Tempra","Uno" ,"Panda", "500", "Doblo","Ducato","Fiorino","Tipo","Diğer"
        ));

        // Honda
        popularMakesAndModels.put("Honda", Arrays.asList(
            "Civic","Accord", "CR-V", "Jazz", "HR-V", "City","Diğer"
        ));

        // Hyundai
        popularMakesAndModels.put("Hyundai", Arrays.asList(
            "i20", "i10","Accent","Accent Blue","Accent Era","H 100","Getz","i30","Bayon","ix35", "Tucson", "Elantra", "Kona","Diğer"
        ));

        // Mercedes-Benz
        popularMakesAndModels.put("Mercedes-Benz", Arrays.asList(
            "A-Serisi", "C-Serisi","S-Serisi", "E-Serisi", "GLA", "CLA","Vito","Diğer"
        ));

        // BMW
        popularMakesAndModels.put("BMW", Arrays.asList(
            "3 Serisi","4 Serisi", "5 Serisi", "X1", "X3","X5", "1 Serisi","Diğer"
        ));

        // Audi
        popularMakesAndModels.put("Audi", Arrays.asList(
            "A3", "A4","A5", "A6", "Q2", "Q3","Q5", "Q7", "Q8","Diğer"
        ));

        // Peugeot
        popularMakesAndModels.put("Peugeot", Arrays.asList(
            "208","206","207","208","Bipper","Boxer","Partner","Rifter", "2008", "301","306","307", "308", "3008","406","407","508","408","5008","Diğer"
        ));

        // Opel
        popularMakesAndModels.put("Opel", Arrays.asList(
            "Corsa","Combo", "Astra", "Mokka", "Crossland", "Grandland","Insigna","Vectra","Diğer"
        ));

        // Mazda
        popularMakesAndModels.put("Mazda", Arrays.asList(
            "3", "6", "CX-3", "CX-30", "CX-5", "CX-60", "MX-30", "MX-5","Diğer"
        ));

        // Chevrolet
        popularMakesAndModels.put("Chevrolet", Arrays.asList(
            "Cruze","Aveo","Captiva", "Malibu", "Trax", "Trailblazer", "Camaro", "Corvette","Diğer"
        ));

        // Citroen
        popularMakesAndModels.put("Citroen", Arrays.asList(
            "C3","Berlingo","Nemo", "C4","C-Elysee", "C5", "Berlingo", "C3 Aircross", "C5 Aircross","C4 Cactus","Diğer"
        ));

        // Dacia
        popularMakesAndModels.put("Dacia", Arrays.asList(
            "Sandero", "Logan", "Duster","Dokker", "Jogger", "Spring","Diğer"
        ));

        // Kia
        popularMakesAndModels.put("Kia", Arrays.asList(
            "Rio", "Ceed", "Sportage", "Sorento", "Picanto", "Stonic", "XCeed","Diğer"
        ));

        // Nissan
        popularMakesAndModels.put("Nissan", Arrays.asList(
            "Qashqai", "Juke", "X-Trail", "Micra", "Leaf", "Navara","Diğer"
        ));

        // Seat
        popularMakesAndModels.put("Seat", Arrays.asList(
            "Ibiza", "Leon", "Ateca", "Arona", "Tarraco","Toledo","Cordoba", "Cupra","Diğer"
        ));

        // Skoda
        popularMakesAndModels.put("Skoda", Arrays.asList(
            "Octavia", "Superb", "Kodiaq","Kamiq","Yeti", "Karoq", "Fabia", "Scala","Diğer"
        ));

        // Tofaş
        popularMakesAndModels.put("Tofaş", Arrays.asList(
            "Doğan", "Kartal", "Şahin", "Serçe", "Murat", "Diğer"
        ));

        // Volvo
        popularMakesAndModels.put("Volvo", Arrays.asList(
            "S60","S40", "S90", "XC40", "XC60", "XC90","V40", "V60", "V90","Diğer"
        ));

        // Chery
        popularMakesAndModels.put("Chery", Arrays.asList(
            "Tiggo 4", "Tiggo 7", "Tiggo 8", "Arrizo 6", "Arrizo 8","Diğer"
        ));

        // Land Rover
        popularMakesAndModels.put("Land Rover", Arrays.asList(
            "Defender", "Discovery", "Discovery Sport", "Range Rover", "Range Rover Sport", "Range Rover Evoque","Diğer"
        ));

        // Mitsubishi
        popularMakesAndModels.put("Mitsubishi", Arrays.asList(
            "L200","L300", "ASX", "Eclipse Cross", "Outlander", "Pajero","Diğer"
        ));

        // SsangYong
        popularMakesAndModels.put("SsangYong", Arrays.asList(
            "Tivoli", "Korando", "Rexton", "Musso", "Musso Grand","Diğer"
        ));

        // Suzuki
        popularMakesAndModels.put("Suzuki", Arrays.asList(
            "Swift", "Vitara", "S-Cross", "Jimny", "Ignis", "Baleno","Diğer"
        ));

        // Jeep
        popularMakesAndModels.put("Jeep", Arrays.asList(
            "Compass", "Renegade", "Grand Cherokee", "Wrangler", "Gladiator","Diğer"
        ));

         // Diğer
         popularMakesAndModels.put("Diğer", Arrays.asList(
            "Diğer"
        ));
        
    }

    public List<String> getAllMakes() {
        List<String> makes = new ArrayList<>(popularMakesAndModels.keySet());
        Collections.sort(makes, String.CASE_INSENSITIVE_ORDER);
        return makes;
    }

    public List<String> getModelsByMake(String make) {
        List<String> models = new ArrayList<>(popularMakesAndModels.getOrDefault(make, new ArrayList<>()));
        // Remove "Diğer" from the list if it exists
        boolean hasDiger = models.remove("Diğer");
        // Sort the remaining models
        Collections.sort(models, String.CASE_INSENSITIVE_ORDER);
        // Add "Diğer" back at the end if it existed
        if (hasDiger) {
            models.add("Diğer");
        }
        return models;
    }
} 