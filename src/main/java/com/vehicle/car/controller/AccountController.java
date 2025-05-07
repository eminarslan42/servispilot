package com.vehicle.car.controller;

import com.vehicle.car.model.Account;
import com.vehicle.car.model.User;
import com.vehicle.car.service.AccountService;
import com.vehicle.car.service.TransactionService;
import com.vehicle.car.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final UserService userService;
    
    // Ensure cash account exists when application starts
    @EventListener(ContextRefreshedEvent.class)
    public void ensureCashAccountExists() {
        // Create a cash account for each user
        List<User> allUsers = userService.getAllUsers();
        for (User user : allUsers) {
            // Check if user already has a "Kasa" account
            List<Account> userCashAccounts = accountService.searchAccountsByUserId("Kasa", user.getId());
            if (userCashAccounts.isEmpty()) {
                // Create a new cash account for this user
                Account cashAccount = new Account();
                cashAccount.setName("Kasa");
                cashAccount.setBalance(BigDecimal.ZERO);
                cashAccount.setUser(user);
                accountService.saveAccount(cashAccount);
            }
        }
    }

    @GetMapping
    public String listAccounts(Model model, @RequestParam(required = false) String query) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                // Kullanıcının kendi hesaplarını göster
                if (query != null && !query.isEmpty()) {
                    model.addAttribute("accounts", accountService.searchAccountsByUserId(query, user.getId()));
                } else {
                    model.addAttribute("accounts", accountService.getAccountsByUserId(user.getId()));
                }
                
                // Find user's cash account (Kasa)
                List<Account> userCashAccounts = accountService.searchAccountsByUserId("Kasa", user.getId());
                if (!userCashAccounts.isEmpty()) {
                    Account cashAccount = userCashAccounts.get(0);
                    model.addAttribute("cashAccount", cashAccount);
                    
                    // Pozitif bakiye (para var) = yeşil, negatif bakiye (borç) = kırmızı
                    model.addAttribute("cashAccountBalanceAbs", cashAccount.getBalance().abs());
                    model.addAttribute("isCashAccountBalanceNegative", cashAccount.getBalance().compareTo(BigDecimal.ZERO) < 0);
                }
            } else if (query != null && !query.isEmpty()) {
                model.addAttribute("accounts", accountService.searchAccounts(query));
            } else {
                model.addAttribute("accounts", accountService.getAllAccounts());
            }
        } else if (query != null && !query.isEmpty()) {
            model.addAttribute("accounts", accountService.searchAccounts(query));
        } else {
            model.addAttribute("accounts", accountService.getAllAccounts());
        }
        
        return "account/list";
    }

    @GetMapping("/new")
    public String showAccountForm(Model model) {
        model.addAttribute("account", new Account());
        return "account/form";
    }

    @PostMapping
    public String saveAccount(@ModelAttribute Account account, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                // Kullanıcı ile ilişkilendir
                account.setUser(user);
            }
        }
        
        accountService.saveAccount(account);
        redirectAttributes.addFlashAttribute("message", "Cari hesap başarıyla kaydedildi!");
        return "redirect:/accounts";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Optional<Account> accountOpt = accountService.getAccountById(id);
        
        // Kasa hesabı ise özel mesaj ekle
        if (accountOpt.isPresent() && accountOpt.get().getName().equalsIgnoreCase("Kasa")) {
            model.addAttribute("kasaWarning", "Dikkat: Bu özel bir Kasa hesabıdır. Hesap adını değiştirmemeniz önerilir.");
        }
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                // Kullanıcının kendi hesabını düzenlemesine izin ver
                accountService.getAccountById(id).ifPresent(account -> {
                    if (account.getUser() != null && !account.getUser().getId().equals(user.getId())) {
                        return; // Başka kullanıcının hesabını düzenlemeye çalışıyor
                    }
                    model.addAttribute("account", account);
                });
            } else {
                accountService.getAccountById(id).ifPresent(account -> model.addAttribute("account", account));
            }
        } else {
            accountService.getAccountById(id).ifPresent(account -> model.addAttribute("account", account));
        }
        return "account/form";
    }

    @GetMapping("/{id}")
    public String viewAccount(@PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                // Kullanıcının kendi hesabını görüntülemesine izin ver
                accountService.getAccountById(id).ifPresent(account -> {
                    if (account.getUser() != null && !account.getUser().getId().equals(user.getId())) {
                        return; // Başka kullanıcının hesabını görüntülemeye çalışıyor
                    }
                    model.addAttribute("account", account);
                    model.addAttribute("transactions", transactionService.getTransactionsByAccountId(id));
                    model.addAttribute("totalCredit", transactionService.getTotalCreditByAccountId(id));
                    model.addAttribute("totalDebit", transactionService.getTotalDebitByAccountId(id));
                });
            } else {
                accountService.getAccountById(id).ifPresent(account -> {
                    model.addAttribute("account", account);
                    model.addAttribute("transactions", transactionService.getTransactionsByAccountId(id));
                    model.addAttribute("totalCredit", transactionService.getTotalCreditByAccountId(id));
                    model.addAttribute("totalDebit", transactionService.getTotalDebitByAccountId(id));
                });
            }
        } else {
            accountService.getAccountById(id).ifPresent(account -> {
                model.addAttribute("account", account);
                model.addAttribute("transactions", transactionService.getTransactionsByAccountId(id));
                model.addAttribute("totalCredit", transactionService.getTotalCreditByAccountId(id));
                model.addAttribute("totalDebit", transactionService.getTotalDebitByAccountId(id));
            });
        }
        return "account/view";
    }

    @PostMapping("/{id}/delete")
    public String deleteAccount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                // Kullanıcının kendi hesabını silmesine izin ver
                accountService.getAccountById(id).ifPresent(account -> {
                    // Kasa hesabını silmeye çalışıyorsa engelle
                    if (account.getName().equalsIgnoreCase("Kasa")) {
                        redirectAttributes.addFlashAttribute("error", "Kasa hesabı silinemez!");
                        return;
                    }
                    
                    if (account.getUser() != null && !account.getUser().getId().equals(user.getId())) {
                        redirectAttributes.addFlashAttribute("error", "Bu hesabı silme yetkiniz yok!");
                        return;
                    }
                    accountService.deleteAccount(id);
                    redirectAttributes.addFlashAttribute("message", "Account deleted successfully!");
                });
            } else {
                // Admin veya hesap sahibi olmayan kullanıcı için
                accountService.getAccountById(id).ifPresent(account -> {
                    // Kasa hesabını silmeye çalışıyorsa engelle
                    if (account.getName().equalsIgnoreCase("Kasa")) {
                        redirectAttributes.addFlashAttribute("error", "Kasa hesabı silinemez!");
                        return;
                    }
                    
                    accountService.deleteAccount(id);
                    redirectAttributes.addFlashAttribute("message", "Account deleted successfully!");
                });
            }
        } else {
            // Oturum açmamış kullanıcı için
            accountService.getAccountById(id).ifPresent(account -> {
                // Kasa hesabını silmeye çalışıyorsa engelle
                if (account.getName().equalsIgnoreCase("Kasa")) {
                    redirectAttributes.addFlashAttribute("error", "Kasa hesabı silinemez!");
                    return;
                }
                
                accountService.deleteAccount(id);
                redirectAttributes.addFlashAttribute("message", "Account deleted successfully!");
            });
        }
        return "redirect:/accounts";
    }

    @PostMapping("/{id}/recalculate")
    public String recalculateAccountBalance(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                try {
                    Account account = accountService.getAccountById(id)
                        .orElseThrow(() -> new IllegalStateException("Hesap bulunamadı"));
                    
                    // Kullanıcının kendi hesabını yeniden hesaplamasına izin ver
                    if (account.getUser() != null && !account.getUser().getId().equals(user.getId())) {
                        redirectAttributes.addFlashAttribute("error", "Bu hesabı yeniden hesaplama yetkiniz yok!");
                        return "redirect:/accounts";
                    }
                    
                    // Kasa hesabı ise tüm işlemlere dayalı olarak hesapla
                    if (account.getName().equalsIgnoreCase("Kasa")) {
                        transactionService.recalculateCashAccountBalance(user.getId());
                        redirectAttributes.addFlashAttribute("message", "Kasa bakiyesi başarıyla yeniden hesaplandı!");
                    } else {
                        // Normal hesap için 
                        transactionService.recalculateAccountBalance(id);
                        redirectAttributes.addFlashAttribute("message", "Hesap bakiyesi başarıyla yeniden hesaplandı!");
                    }
                    
                    return "redirect:/accounts/" + id;
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("error", "Bakiye hesaplanırken bir hata oluştu: " + e.getMessage());
                    return "redirect:/accounts";
                }
            }
        }
        
        redirectAttributes.addFlashAttribute("error", "Bu işlemi gerçekleştirmek için giriş yapmalısınız!");
        return "redirect:/accounts";
    }
    
    @GetMapping("/cash/recalculate")
    public String recalculateCashBalance(RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                try {
                    // Kullanıcının kasa hesabını bul
                    List<Account> userCashAccounts = accountService.searchAccountsByUserId("Kasa", user.getId());
                    if (userCashAccounts.isEmpty()) {
                        redirectAttributes.addFlashAttribute("error", "Kasa hesabı bulunamadı!");
                        return "redirect:/accounts";
                    }
                    
                    // Kasa bakiyesini yeniden hesapla
                    transactionService.recalculateCashAccountBalance(user.getId());
                    
                    redirectAttributes.addFlashAttribute("message", "Kasa bakiyesi başarıyla yeniden hesaplandı!");
                    return "redirect:/accounts";
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("error", "Kasa bakiyesi hesaplanırken bir hata oluştu: " + e.getMessage());
                    return "redirect:/accounts";
                }
            }
        }
        
        redirectAttributes.addFlashAttribute("error", "Bu işlemi gerçekleştirmek için giriş yapmalısınız!");
        return "redirect:/accounts";
    }
} 