package com.vehicle.car.service;

import com.vehicle.car.model.Account;
import com.vehicle.car.model.Transaction;
import com.vehicle.car.model.Transaction.TransactionType;
import com.vehicle.car.model.User;
import com.vehicle.car.repository.AccountRepository;
import com.vehicle.car.repository.TransactionRepository;
import com.vehicle.car.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    public List<Transaction> getTransactionsByAccountId(Long accountId) {
        return transactionRepository.findByAccountIdOrderByTransactionDateDesc(accountId);
    }
    
    public List<Transaction> getTransactionsByUserId(Long userId) {
        return transactionRepository.findByUserIdOrderByTransactionDateDesc(userId);
    }
    
    public List<Transaction> getTransactionsByUser(User user) {
        return transactionRepository.findByUser(user);
    }

    public BigDecimal getTotalCreditByAccountId(Long accountId) {
        return transactionRepository.findByAccountIdAndType(accountId, TransactionType.CREDIT)
            .stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalDebitByAccountId(Long accountId) {
        return transactionRepository.findByAccountIdAndType(accountId, TransactionType.DEBIT)
            .stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal getTotalCreditByUserId(Long userId) {
        return transactionRepository.findByUserIdAndType(userId, TransactionType.CREDIT)
            .stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalDebitByUserId(Long userId) {
        return transactionRepository.findByUserIdAndType(userId, TransactionType.DEBIT)
            .stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public Transaction saveTransaction(Transaction transaction) {
        Account account = transaction.getAccount();
        
        // Update account balance based on transaction type
        if (transaction.getType() == TransactionType.CREDIT) {
            account.setBalance(account.getBalance().add(transaction.getAmount()));
        } else {
            account.setBalance(account.getBalance().subtract(transaction.getAmount()));
        }
        
        accountRepository.save(account);
        return transactionRepository.save(transaction);
    }
    
    @Transactional
    public Transaction saveTransactionForUser(Transaction transaction, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("Kullanıcı bulunamadı"));
        transaction.setUser(user);
        
        Account account = transaction.getAccount();
        
        // Update account balance based on transaction type
        if (transaction.getType() == TransactionType.CREDIT) {
            account.setBalance(account.getBalance().add(transaction.getAmount()));
        } else {
            account.setBalance(account.getBalance().subtract(transaction.getAmount()));
        }
        
        accountRepository.save(account);
        return transactionRepository.save(transaction);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        Optional<Transaction> transaction = transactionRepository.findById(id);
        if (transaction.isPresent()) {
            Account account = transaction.get().getAccount();
            
            // Reverse the balance change
            if (transaction.get().getType() == TransactionType.CREDIT) {
                account.setBalance(account.getBalance().subtract(transaction.get().getAmount()));
            } else {
                account.setBalance(account.getBalance().add(transaction.get().getAmount()));
            }
            
            accountRepository.save(account);
            transactionRepository.deleteById(id);
        }
    }
} 