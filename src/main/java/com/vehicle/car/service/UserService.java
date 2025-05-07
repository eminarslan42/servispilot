package com.vehicle.car.service;

import com.vehicle.car.model.Account;
import com.vehicle.car.model.User;
import com.vehicle.car.repository.AccountRepository;
import com.vehicle.car.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElse(null);
    }
    
    public Optional<User> findByUsernameAndEmail(String username, String email) {
        return userRepository.findByUsernameAndEmail(username, email);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User saveUser(User user) {
        User savedUser = userRepository.save(user);
        createCashAccountForUser(savedUser);
        return savedUser;
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    /**
     * Admin tarafından şifre sıfırlama işlemi.
     * Bu işlem sadece admin yetkileri ile kullanılmalıdır.
     *
     * @param username Kullanıcı adı
     * @param email Kullanıcı e-postası  
     * @return Rastgele üretilen yeni şifre. Kullanıcı bulunamazsa null döner.
     */
    @Transactional
    public String resetPassword(String username, String email) {
        Optional<User> userOpt = findByUsernameAndEmail(username, email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Rastgele şifre oluştur
            String newPassword = UUID.randomUUID().toString().substring(0, 8);
            
            // Şifreyi hashle ve kaydet
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            
            return newPassword;
        }
        
        return null;
    }
    
    // Helper method to create a cash account for a new user
    private void createCashAccountForUser(User user) {
        // Check if user already has a cash account
        List<Account> userCashAccounts = accountRepository.findByNameContainingIgnoreCaseAndUserId("Kasa", user.getId());
        if (userCashAccounts.isEmpty()) {
            // Create a new cash account for this user
            Account cashAccount = new Account();
            cashAccount.setName("Kasa");
            cashAccount.setBalance(BigDecimal.ZERO);
            cashAccount.setUser(user);
            accountRepository.save(cashAccount);
        }
    }
} 