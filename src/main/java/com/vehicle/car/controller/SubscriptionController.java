package com.vehicle.car.controller;

import com.vehicle.car.model.Subscription;
import com.vehicle.car.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@Controller
@RequestMapping("/api/subscriptions")
public class SubscriptionController {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);

    @Autowired
    private SubscriptionService subscriptionService;

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Subscription>> getActiveSubscriptions() {
        List<Subscription> activeSubscriptions = subscriptionService.findActiveSubscriptions();
        return ResponseEntity.ok(activeSubscriptions);
    }

    @GetMapping("/expired")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Subscription>> getExpiredSubscriptions() {
        List<Subscription> expiredSubscriptions = subscriptionService.findExpiredSubscriptions();
        return ResponseEntity.ok(expiredSubscriptions);
    }

    @PostMapping("/{userId}/extend")
    public String extendSubscription(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            logger.info("Extending subscription for user ID: {}", userId);
            subscriptionService.extendSubscription(userId);
            redirectAttributes.addFlashAttribute("success", "Abonelik başarıyla uzatıldı.");
        } catch (Exception e) {
            logger.error("Error extending subscription for user ID: {}", userId, e);
            redirectAttributes.addFlashAttribute("error", "Abonelik uzatılırken bir hata oluştu: " + e.getMessage());
        }
        return "redirect:/admin/subscriptions";
    }

    @PostMapping("/{userId}/renew")
    public String renewSubscription(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            logger.info("Renewing subscription for user ID: {}", userId);
            subscriptionService.renewSubscription(userId);
            redirectAttributes.addFlashAttribute("success", "Abonelik başarıyla yenilendi.");
        } catch (Exception e) {
            logger.error("Error renewing subscription for user ID: {}", userId, e);
            redirectAttributes.addFlashAttribute("error", "Abonelik yenilenirken bir hata oluştu: " + e.getMessage());
        }
        return "redirect:/admin/subscriptions";
    }

    @PostMapping("/{userId}/cancel")
    public String cancelSubscription(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            logger.info("Canceling subscription for user ID: {}", userId);
            subscriptionService.cancelSubscription(userId);
            redirectAttributes.addFlashAttribute("success", "Abonelik başarıyla iptal edildi.");
        } catch (Exception e) {
            logger.error("Error canceling subscription for user ID: {}", userId, e);
            redirectAttributes.addFlashAttribute("error", "Abonelik iptal edilirken bir hata oluştu: " + e.getMessage());
        }
        return "redirect:/admin/subscriptions";
    }

    @PostMapping("/deactivate/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deactivateSubscription(@PathVariable Long userId) {
        try {
            subscriptionService.deactivateSubscription(userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/status/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> checkSubscriptionStatus(@PathVariable Long userId) {
        try {
            boolean isValid = subscriptionService.isSubscriptionValid(userId);
            return ResponseEntity.ok(new SubscriptionStatusResponse(isValid));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/deactivate-expired")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deactivateExpiredSubscriptions() {
        try {
            subscriptionService.deactivateExpiredSubscriptions();
            return ResponseEntity.ok("Expired subscriptions have been deactivated");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

class SubscriptionStatusResponse {
    private boolean active;

    public SubscriptionStatusResponse(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
} 