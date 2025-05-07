package com.vehicle.car.repository;

import com.vehicle.car.model.Transaction;
import com.vehicle.car.model.Transaction.TransactionType;
import com.vehicle.car.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountIdOrderByTransactionDateDesc(Long accountId);
    List<Transaction> findByAccountIdAndType(Long accountId, TransactionType type);
    
    // New methods for user relationship
    List<Transaction> findByUser(User user);
    List<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId);
    List<Transaction> findByUserIdAndType(Long userId, TransactionType type);
    
    // Method to delete all transactions for an account
    @Modifying
    @Transactional
    @Query("DELETE FROM Transaction t WHERE t.account.id = ?1")
    void deleteByAccountId(Long accountId);
}