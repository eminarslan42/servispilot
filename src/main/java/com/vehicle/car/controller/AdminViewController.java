package com.vehicle.car.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import com.vehicle.car.service.AccountService;
import com.vehicle.car.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import com.vehicle.car.model.Subscription;

@Controller
public class AdminViewController {
    private static final Logger logger = LoggerFactory.getLogger(AdminViewController.class);

    @Autowired
    private AccountService accountService;

    @Autowired
    private SubscriptionService subscriptionService;

    @GetMapping("/admin/subscriptions")
    public String subscriptionManagement(Model model) {
        List<Subscription> subscriptions = subscriptionService.getAllSubscriptions();
        logger.info("Found {} subscriptions", subscriptions.size());
        subscriptions.forEach(sub -> {
            logger.info("Subscription: userId={}, active={}, endDate={}", 
                sub.getUser().getId(), sub.getIsActive(), sub.getEndDate());
        });
        model.addAttribute("subscriptions", subscriptions);
        return "admin/subscription-management";
    }

    @GetMapping("/admin/customers")
    public String customerManagement(Model model) {
        model.addAttribute("customers", accountService.getAllAccounts());
        return "admin/customer-management";
    }

    @GetMapping("/admin/customers/new")
    public String newCustomerForm() {
        return "admin/customer-form";
    }
} 