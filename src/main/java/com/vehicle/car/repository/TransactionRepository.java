package com.vehicle.car.repository;

import com.vehicle.car.model.Transaction;
import com.vehicle.car.model.Transaction.TransactionType;
import com.vehicle.car.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountIdOrderByTransactionDateDesc(Long accountId);
    List<Transaction> findByAccountIdAndType(Long accountId, TransactionType type);
    
    // New methods for user relationship
    List<Transaction> findByUser(User user);
    List<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId);
    List<Transaction> findByUserIdAndType(Long userId, TransactionType type);
} 