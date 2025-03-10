package com.vehicle.car.service;

import com.vehicle.car.model.Account;
import com.vehicle.car.model.User;
import com.vehicle.car.repository.AccountRepository;
import com.vehicle.car.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Optional<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }
    
    public List<Account> getAccountsByUserId(Long userId) {
        return accountRepository.findAllByUserId(userId);
    }
    
    public List<Account> getAccountsByUser(User user) {
        return accountRepository.findAllByUser(user);
    }

    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }
    
    public Account saveAccountForUser(Account account, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("Kullanıcı bulunamadı"));
        account.setUser(user);
        return accountRepository.save(account);
    }

    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
    }

    public List<Account> searchAccounts(String query) {
        return accountRepository.findByNameContainingIgnoreCase(query);
    }
    
    public List<Account> searchAccountsByUserId(String query, Long userId) {
        return accountRepository.findByNameContainingIgnoreCaseAndUserId(query, userId);
    }
} 