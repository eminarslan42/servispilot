package com.vehicle.car.service;

import com.vehicle.car.model.Part;
import com.vehicle.car.model.PartMovement;
import com.vehicle.car.model.User;
import com.vehicle.car.repository.PartMovementRepository;
import com.vehicle.car.repository.PartRepository;
import com.vehicle.car.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PartService {
    private final PartRepository partRepository;
    private final PartMovementRepository partMovementRepository;
    private final UserRepository userRepository;

    public List<Part> getAllParts() {
        return partRepository.findAll();
    }

    public List<Part> getActiveParts() {
        return partRepository.findByIsActiveTrue();
    }
    
    public List<Part> getPartsByUserId(Long userId) {
        return partRepository.findByUserId(userId);
    }
    
    public List<Part> getActivePartsByUserId(Long userId) {
        return partRepository.findByUserIdAndIsActiveTrue(userId);
    }

    public Optional<Part> getPartById(Long id) {
        return partRepository.findById(id);
    }

    public Optional<Part> getPartByCode(String code) {
        return partRepository.findByCode(code);
    }
    
    public Optional<Part> getPartByCodeAndUserId(String code, Long userId) {
        return partRepository.findByCodeAndUserId(code, userId);
    }

    public List<Part> getLowStockParts() {
        return partRepository.findByQuantityLessThanEqualAndIsActiveTrue(0);
    }
    
    public List<Part> getLowStockPartsByUserId(Long userId) {
        return partRepository.findByUserIdAndQuantityLessThanEqual(userId, 0);
    }

    public List<Part> getPartsByCategory(String category) {
        return partRepository.findByCategory(category);
    }
    
    public List<Part> getPartsByCategoryAndUserId(String category, Long userId) {
        return partRepository.findByUserIdAndCategory(userId, category);
    }

    public List<Part> getPartsBySupplier(String supplier) {
        return partRepository.findBySupplier(supplier);
    }
    
    public List<Part> getPartsBySupplierAndUserId(String supplier, Long userId) {
        return partRepository.findByUserIdAndSupplier(userId, supplier);
    }

    public List<Part> searchParts(String query) {
        return partRepository.findByNameContainingIgnoreCase(query);
    }
    
    public List<Part> searchPartsByUserId(String query, Long userId) {
        return partRepository.findByUserIdAndNameContainingIgnoreCase(userId, query);
    }

    @Transactional
    public Part savePart(Part part) {
        // Eğer kullanıcı atanmamışsa, varsayılan olarak admin kullanıcısını ata
        if (part.getUser() == null) {
            userRepository.findByUsername("admin").ifPresent(part::setUser);
        }
        
        return partRepository.save(part);
    }
    
    @Transactional
    public Part savePartForUser(Part part, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("Kullanıcı bulunamadı"));
        
        // Aynı kodlu parça var mı kontrol et
        if (part.getId() == null && part.getCode() != null) {
            Optional<Part> existingPart = partRepository.findByCodeAndUserId(part.getCode(), userId);
            if (existingPart.isPresent()) {
                throw new IllegalStateException("Bu parça kodu zaten kullanılıyor: " + part.getCode());
            }
        }
        
        part.setUser(user);
        return partRepository.save(part);
    }

    @Transactional
    public void deletePart(Long id) {
        partRepository.deleteById(id);
    }

    @Transactional
    public void togglePartStatus(Long id) {
        partRepository.findById(id).ifPresent(part -> {
            part.setIsActive(!part.getIsActive());
            partRepository.save(part);
        });
    }

    @Transactional
    public PartMovement addStock(Long partId, Integer quantity, String reason, String documentNo, String notes) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Miktar 0'dan büyük olmalıdır");
        }

        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new IllegalArgumentException("Parça bulunamadı"));

        PartMovement movement = new PartMovement();
        movement.setPart(part);
        movement.setMovementType("GİRİŞ");
        movement.setQuantity(quantity);
        movement.setReason(reason);
        movement.setUnitPrice(part.getPurchasePrice());
        movement.setDocumentNo(documentNo);
        movement.setNotes(notes);

        // Stok miktarını güncelle
        part.setQuantity(part.getQuantity() + quantity);
        part.setLastStockUpdate(LocalDateTime.now());
        partRepository.save(part);

        return partMovementRepository.save(movement);
    }
    
    @Transactional
    public PartMovement addStockForUser(Long partId, Integer quantity, String reason, String documentNo, String notes, Long userId) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Miktar 0'dan büyük olmalıdır");
        }

        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new IllegalArgumentException("Parça bulunamadı"));
                
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("Kullanıcı bulunamadı"));
            
        // Kullanıcı kontrolü
        if (part.getUser() != null && !part.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Bu parça üzerinde işlem yapma yetkiniz yok");
        }

        PartMovement movement = new PartMovement();
        movement.setPart(part);
        movement.setMovementType("GİRİŞ");
        movement.setQuantity(quantity);
        movement.setReason(reason);
        movement.setUnitPrice(part.getPurchasePrice());
        movement.setDocumentNo(documentNo);
        movement.setNotes(notes);
        movement.setUser(user);

        // Stok miktarını güncelle
        part.setQuantity(part.getQuantity() + quantity);
        part.setLastStockUpdate(LocalDateTime.now());
        partRepository.save(part);

        return partMovementRepository.save(movement);
    }

    @Transactional
    public PartMovement removeStock(Long partId, Integer quantity, String reason, String documentNo, String notes) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Miktar 0'dan büyük olmalıdır");
        }

        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new IllegalArgumentException("Parça bulunamadı"));

        if (part.getQuantity() < quantity) {
            throw new IllegalArgumentException("Stokta yeterli miktarda parça yok");
        }

        PartMovement movement = new PartMovement();
        movement.setPart(part);
        movement.setMovementType("ÇIKIŞ");
        movement.setQuantity(quantity);
        movement.setReason(reason);
        movement.setUnitPrice(part.getSellingPrice() != null ? part.getSellingPrice() : part.getPurchasePrice());
        movement.setDocumentNo(documentNo);
        movement.setNotes(notes);

        // Stok miktarını güncelle
        part.setQuantity(part.getQuantity() - quantity);
        part.setLastStockUpdate(LocalDateTime.now());
        partRepository.save(part);

        return partMovementRepository.save(movement);
    }
    
    @Transactional
    public PartMovement removeStockForUser(Long partId, Integer quantity, String reason, String documentNo, String notes, Long userId) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Miktar 0'dan büyük olmalıdır");
        }

        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new IllegalArgumentException("Parça bulunamadı"));
                
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("Kullanıcı bulunamadı"));
            
        // Kullanıcı kontrolü
        if (part.getUser() != null && !part.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Bu parça üzerinde işlem yapma yetkiniz yok");
        }

        if (part.getQuantity() < quantity) {
            throw new IllegalArgumentException("Stokta yeterli miktarda parça yok");
        }

        PartMovement movement = new PartMovement();
        movement.setPart(part);
        movement.setMovementType("ÇIKIŞ");
        movement.setQuantity(quantity);
        movement.setReason(reason);
        movement.setUnitPrice(part.getSellingPrice() != null ? part.getSellingPrice() : part.getPurchasePrice());
        movement.setDocumentNo(documentNo);
        movement.setNotes(notes);
        movement.setUser(user);

        // Stok miktarını güncelle
        part.setQuantity(part.getQuantity() - quantity);
        part.setLastStockUpdate(LocalDateTime.now());
        partRepository.save(part);

        return partMovementRepository.save(movement);
    }

    public List<PartMovement> getPartMovements(Long partId) {
        return partMovementRepository.findByPartId(partId);
    }
    
    public List<PartMovement> getPartMovementsByUserId(Long userId) {
        return partMovementRepository.findByUserId(userId);
    }
    
    public List<PartMovement> getPartMovementsByPartIdAndUserId(Long partId, Long userId) {
        return partMovementRepository.findByUserIdAndPartId(userId, partId);
    }

    public List<PartMovement> getPartMovementsByDateRange(Long partId, LocalDateTime start, LocalDateTime end) {
        return partMovementRepository.findByPartIdAndCreatedAtBetween(partId, start, end);
    }
    
    public List<PartMovement> getPartMovementsByDateRangeAndUserId(Long partId, LocalDateTime start, LocalDateTime end, Long userId) {
        return partMovementRepository.findByUserIdAndPartIdAndCreatedAtBetween(userId, partId, start, end);
    }
} 