package com.vehicle.car.repository;

import com.vehicle.car.model.Account;
import com.vehicle.car.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByNameContainingIgnoreCase(String name);
    
    // User relationship methods
    List<Account> findAllByUser(User user);
    List<Account> findAllByUserId(Long userId);
    List<Account> findByNameContainingIgnoreCaseAndUserId(String name, Long userId);
} 