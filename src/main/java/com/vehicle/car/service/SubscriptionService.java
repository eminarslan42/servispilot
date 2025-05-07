package com.vehicle.car.service;

import com.vehicle.car.model.Subscription;
import com.vehicle.car.model.User;
import com.vehicle.car.repository.SubscriptionRepository;
import com.vehicle.car.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubscriptionService {
    
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    
    @Autowired
    private UserRepository userRepository;

    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    @Transactional
    public Subscription createSubscription(User user) {
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(LocalDateTime.now().plusMonths(1));
        subscription.setIsActive(true);
        subscription.setLastPaymentDate(LocalDateTime.now());
        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public Subscription renewSubscription(Long userId) {
        return renewSubscription(userId, 1); // Eski metot 1 aylık yenileme yapar
    }
    
    @Transactional
    public Subscription renewSubscription(Long userId, Integer months) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Subscription subscription = user.getSubscription();
        if (subscription == null) {
            Subscription newSubscription = createSubscription(user);
            if (months > 1) {
                // Başlangıçta 1 ay verildiği için kalan ayları ekliyoruz
                newSubscription.setEndDate(newSubscription.getEndDate().plusMonths(months - 1));
            }
            return subscriptionRepository.save(newSubscription);
        }
        
        LocalDateTime now = LocalDateTime.now();
        subscription.setLastPaymentDate(now);
        subscription.setStartDate(now); // Yenilemede başlangıç tarihi şimdiki zaman olur
        subscription.setEndDate(now.plusMonths(months));
        subscription.setIsActive(true);
        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public Subscription extendSubscription(Long userId) {
        return extendSubscription(userId, 1); // Eski metot 1 aylık uzatma yapar
    }
    
    @Transactional
    public Subscription extendSubscription(Long userId, Integer months) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Subscription subscription = user.getSubscription();
        if (subscription == null) {
            Subscription newSubscription = createSubscription(user);
            if (months > 1) {
                // Başlangıçta 1 ay verildiği için kalan ayları ekliyoruz
                newSubscription.setEndDate(newSubscription.getEndDate().plusMonths(months - 1));
            }
            return subscriptionRepository.save(newSubscription);
        }
        
        LocalDateTime now = LocalDateTime.now();
        subscription.setLastPaymentDate(now);
        
        // Mevcut bitiş tarihinden itibaren veya şu andan itibaren uzatma yapılır
        LocalDateTime endDate = subscription.getEndDate();
        if (endDate.isBefore(now)) {
            endDate = now;
        }
        
        subscription.setEndDate(endDate.plusMonths(months));
        subscription.setIsActive(true);
        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public Subscription cancelSubscription(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Subscription subscription = user.getSubscription();
        if (subscription != null) {
            subscription.setIsActive(false);
            return subscriptionRepository.save(subscription);
        }
        throw new RuntimeException("User has no active subscription");
    }

    public List<Subscription> findExpiredSubscriptions() {
        return subscriptionRepository.findByEndDateBeforeAndIsActiveTrue(LocalDateTime.now());
    }

    public List<Subscription> findActiveSubscriptions() {
        return subscriptionRepository.findByIsActiveTrue();
    }

    @Transactional
    public void deactivateExpiredSubscriptions() {
        List<Subscription> expiredSubscriptions = findExpiredSubscriptions();
        for (Subscription subscription : expiredSubscriptions) {
            subscription.setIsActive(false);
            subscriptionRepository.save(subscription);
        }
    }

    @Transactional
    public void deactivateSubscription(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Subscription subscription = user.getSubscription();
        if (subscription != null) {
            subscription.setIsActive(false);
            subscriptionRepository.save(subscription);
        }
    }

    public boolean isSubscriptionValid(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.hasActiveSubscription();
    }
} 