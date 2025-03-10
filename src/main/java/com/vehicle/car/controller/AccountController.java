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


@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final UserService userService;

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
    public String showEditForm(@PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
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
                    if (account.getUser() != null && !account.getUser().getId().equals(user.getId())) {
                        redirectAttributes.addFlashAttribute("error", "Bu hesabı silme yetkiniz yok!");
                        return;
                    }
                    accountService.deleteAccount(id);
                    redirectAttributes.addFlashAttribute("message", "Account deleted successfully!");
                });
            } else {
                accountService.deleteAccount(id);
                redirectAttributes.addFlashAttribute("message", "Account deleted successfully!");
            }
        } else {
            accountService.deleteAccount(id);
            redirectAttributes.addFlashAttribute("message", "Account deleted successfully!");
        }
        return "redirect:/accounts";
    }
} 