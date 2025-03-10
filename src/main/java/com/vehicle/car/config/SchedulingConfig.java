package com.vehicle.car.config;

import com.vehicle.car.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class SchedulingConfig {

    @Autowired
    private SubscriptionService subscriptionService;

    // Her gün gece yarısı çalışır
    @Scheduled(cron = "0 0 0 * * ?")
    public void checkExpiredSubscriptions() {
        subscriptionService.deactivateExpiredSubscriptions();
    }
} 