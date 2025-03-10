package com.vehicle.car.repository;

import com.vehicle.car.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByEndDateBeforeAndIsActiveTrue(LocalDateTime date);
    List<Subscription> findByIsActiveTrue();
} 