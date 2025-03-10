package com.vehicle.car.repository;

import com.vehicle.car.model.Part;
import com.vehicle.car.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PartRepository extends JpaRepository<Part, Long> {
    Optional<Part> findByCode(String code);
    List<Part> findByIsActiveTrue();
    List<Part> findByQuantityLessThanEqualAndIsActiveTrue(Integer quantity);
    List<Part> findByCategory(String category);
    List<Part> findBySupplier(String supplier);
    List<Part> findByNameContainingIgnoreCase(String name);
    
    // User ilişkisi için yeni metodlar
    List<Part> findByUser(User user);
    List<Part> findByUserId(Long userId);
    List<Part> findByUserIdAndIsActiveTrue(Long userId);
    List<Part> findByUserIdAndQuantityLessThanEqual(Long userId, Integer quantity);
    List<Part> findByUserIdAndCategory(Long userId, String category);
    List<Part> findByUserIdAndSupplier(Long userId, String supplier);
    List<Part> findByUserIdAndNameContainingIgnoreCase(Long userId, String name);
    Optional<Part> findByCodeAndUserId(String code, Long userId);
} 