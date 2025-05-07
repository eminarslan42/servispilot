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
        
        // Update cash account
        updateCashAccount(transaction);
        
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
        
        // Update cash account
        updateCashAccount(transaction);
        
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
            
            // Reverse cash account update
            reverseCashAccountUpdate(transaction.get());
            
            transactionRepository.deleteById(id);
        }
    }
    
    // Helper method to update cash account
    private void updateCashAccount(Transaction transaction) {
        // Get the user from the transaction
        User user = transaction.getUser();
        if (user == null) {
            // If transaction doesn't have a user, try to get it from the account
            if (transaction.getAccount() != null && transaction.getAccount().getUser() != null) {
                user = transaction.getAccount().getUser();
            } else {
                // If no user found, we can't update a specific cash account
                return;
            }
        }
        
        // Find the user's cash account (Kasa)
        List<Account> userCashAccounts = accountRepository.findByNameContainingIgnoreCaseAndUserId("Kasa", user.getId());
        if (userCashAccounts.isEmpty()) {
            // Create cash account for this user if it doesn't exist
            Account cashAccount = new Account();
            cashAccount.setName("Kasa");
            cashAccount.setBalance(BigDecimal.ZERO);
            cashAccount.setUser(user);
            cashAccount = accountRepository.save(cashAccount);
            userCashAccounts = List.of(cashAccount);
        }
        
        Account cashAccount = userCashAccounts.get(0);
        
        // ÖNEMLİ: Eğer işlem zaten kasa hesabına yapılıyorsa, tekrar kasa hesabını güncelleme (çift sayımı önle)
        if (transaction.getAccount() != null && 
            transaction.getAccount().getId() != null && 
            transaction.getAccount().getId().equals(cashAccount.getId())) {
            return;
        }
        
        // Düzeltilmiş mantık: 
        // CREDIT (Alacak): Para alındı, kasa artar (+)
        // DEBIT (Borç): Para verildi, kasa azalır (-)
        if (transaction.getType() == TransactionType.CREDIT) {
            // Müşteriden para alındı - kasa artar
            cashAccount.setBalance(cashAccount.getBalance().add(transaction.getAmount()));
        } else {
            // Müşteriye para verildi - kasa azalır
            cashAccount.setBalance(cashAccount.getBalance().subtract(transaction.getAmount()));
        }
        
        accountRepository.save(cashAccount);
    }
    
    // Helper method to reverse cash account update when a transaction is deleted
    private void reverseCashAccountUpdate(Transaction transaction) {
        // Get the user from the transaction
        User user = transaction.getUser();
        if (user == null) {
            // If transaction doesn't have a user, try to get it from the account
            if (transaction.getAccount() != null && transaction.getAccount().getUser() != null) {
                user = transaction.getAccount().getUser();
            } else {
                // If no user found, we can't update a specific cash account
                return;
            }
        }
        
        // Find the user's cash account (Kasa)
        List<Account> userCashAccounts = accountRepository.findByNameContainingIgnoreCaseAndUserId("Kasa", user.getId());
        if (!userCashAccounts.isEmpty()) {
            Account cashAccount = userCashAccounts.get(0);
            
            // ÖNEMLİ: Eğer işlem zaten kasa hesabına yapılıyorsa, tekrar kasa hesabını güncelleme (çift sayımı önle)
            if (transaction.getAccount() != null && 
                transaction.getAccount().getId() != null && 
                transaction.getAccount().getId().equals(cashAccount.getId())) {
                return;
            }
            
            // Düzeltilmiş mantığın tersine çevrilmesi:
            if (transaction.getType() == TransactionType.CREDIT) {
                // Tersine çevir: Müşteriden para alındı - kasa azalır
                cashAccount.setBalance(cashAccount.getBalance().subtract(transaction.getAmount()));
            } else {
                // Tersine çevir: Müşteriye para verildi - kasa artar
                cashAccount.setBalance(cashAccount.getBalance().add(transaction.getAmount()));
            }
            
            accountRepository.save(cashAccount);
        }
    }

    /**
     * Hesap bakiyesini mevcut işlemlerine göre yeniden hesaplar.
     * Bu metot, hesap bakiyesindeki tutarsızlıkları düzeltmek için kullanılabilir.
     * 
     * @param accountId Bakiyesi düzeltilecek hesabın ID'si
     * @return Güncellenen hesap
     */
    @Transactional
    public Account recalculateAccountBalance(Long accountId) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (!accountOpt.isPresent()) {
            throw new IllegalStateException("Hesap bulunamadı: " + accountId);
        }
        
        Account account = accountOpt.get();
        
        // İşlemleri al
        List<Transaction> transactions = transactionRepository.findByAccountIdOrderByTransactionDateDesc(accountId);
        
        // Bakiyeyi sıfırla
        account.setBalance(BigDecimal.ZERO);
        
        // Tüm işlemleri hesapla
        for (Transaction transaction : transactions) {
            if (transaction.getType() == TransactionType.CREDIT) {
                account.setBalance(account.getBalance().add(transaction.getAmount()));
            } else {
                account.setBalance(account.getBalance().subtract(transaction.getAmount()));
            }
        }
        
        // Hesabı kaydet
        return accountRepository.save(account);
    }
    
    /**
     * Kasa hesabının bakiyesini mevcut işlemlerine göre yeniden hesaplar.
     * 
     * @param userId Kullanıcı ID'si
     * @return Güncellenen kasa hesabı
     */
    @Transactional
    public Account recalculateCashAccountBalance(Long userId) {
        // Kullanıcının kasa hesabını bul
        List<Account> userCashAccounts = accountRepository.findByNameContainingIgnoreCaseAndUserId("Kasa", userId);
        if (userCashAccounts.isEmpty()) {
            throw new IllegalStateException("Kasa hesabı bulunamadı. Kullanıcı ID: " + userId);
        }
        
        Account cashAccount = userCashAccounts.get(0);
        
        // Kullanıcının tüm işlemlerini al
        List<Transaction> allTransactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(userId);
        
        // Kasa bakiyesini sıfırla
        cashAccount.setBalance(BigDecimal.ZERO);
        
        for (Transaction transaction : allTransactions) {
            // Kasa hesabının kendi işlemlerini atla - zaten account hesaplamasında dikkate alındı
            if (transaction.getAccount() != null && 
                transaction.getAccount().getId() != null && 
                transaction.getAccount().getId().equals(cashAccount.getId())) {
                continue;
            }
            
            // İşlem tipine göre kasa bakiyesini güncelle
            if (transaction.getType() == TransactionType.CREDIT) {
                // Alacak - kasaya para girişi
                cashAccount.setBalance(cashAccount.getBalance().add(transaction.getAmount()));
            } else {
                // Borç - kasadan para çıkışı
                cashAccount.setBalance(cashAccount.getBalance().subtract(transaction.getAmount()));
            }
        }
        
        // Kasa hesabını kaydet
        return accountRepository.save(cashAccount);
    }
} 