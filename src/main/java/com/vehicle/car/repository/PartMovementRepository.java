package com.vehicle.car.repository;

import com.vehicle.car.model.PartMovement;
import com.vehicle.car.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface PartMovementRepository extends JpaRepository<PartMovement, Long> {
    List<PartMovement> findByPartId(Long partId);
    List<PartMovement> findByPartIdAndCreatedAtBetween(Long partId, LocalDateTime start, LocalDateTime end);
    List<PartMovement> findByServiceRecordId(Long serviceRecordId);
    List<PartMovement> findByMovementType(String movementType);
    
    // User ilişkisi için yeni metodlar
    List<PartMovement> findByUser(User user);
    List<PartMovement> findByUserId(Long userId);
    List<PartMovement> findByUserIdAndPartId(Long userId, Long partId);
    List<PartMovement> findByUserIdAndMovementType(Long userId, String movementType);
    List<PartMovement> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
    List<PartMovement> findByUserIdAndPartIdAndCreatedAtBetween(Long userId, Long partId, LocalDateTime start, LocalDateTime end);
} 