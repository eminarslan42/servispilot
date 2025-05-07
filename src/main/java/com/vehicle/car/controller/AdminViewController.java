package com.vehicle.car.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import com.vehicle.car.service.AccountService;
import com.vehicle.car.service.SubscriptionService;
import com.vehicle.car.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import com.vehicle.car.model.Subscription;
import com.vehicle.car.model.User;

@Controller
public class AdminViewController {
    private static final Logger logger = LoggerFactory.getLogger(AdminViewController.class);

    @Autowired
    private AccountService accountService;

    @Autowired
    private SubscriptionService subscriptionService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/admin/subscriptions")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    public String customerManagement(Model model) {
        model.addAttribute("customers", accountService.getAllAccounts());
        return "admin/customer-management";
    }

    @GetMapping("/admin/customers/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newCustomerForm() {
        return "admin/customer-form";
    }
    
    /**
     * Şifre sıfırlama sayfasını gösterir.
     * Sadece admin yetkisine sahip kullanıcılar erişebilir.
     * 
     * @param model Model nesnesi
     * @return Şifre sıfırlama sayfasının yolu
     */
    @GetMapping("/admin/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public String resetPasswordPage(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/reset-password";
    }
} 