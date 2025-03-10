package com.vehicle.car.controller;

import com.vehicle.car.model.Transaction;
import com.vehicle.car.model.User;
import com.vehicle.car.model.Account;
import com.vehicle.car.service.AccountService;
import com.vehicle.car.service.TransactionService;
import com.vehicle.car.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final UserService userService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(LocalDateTime.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                if (text == null || text.trim().isEmpty()) {
                    setValue(null);
                } else {
                    try {
                        LocalDate date = LocalDate.parse(text, DateTimeFormatter.ISO_DATE);
                        setValue(date.atStartOfDay());
                    } catch (Exception e) {
                        setValue(LocalDateTime.now());
                    }
                }
            }

            @Override
            public String getAsText() {
                LocalDateTime value = (LocalDateTime) getValue();
                if (value == null) {
                    return "";
                }
                return value.format(DateTimeFormatter.ISO_DATE);
            }
        });
    }

    @GetMapping
    public String listTransactions(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                // Kullanıcının kendi işlemlerini göster
                model.addAttribute("transactions", transactionService.getTransactionsByUserId(user.getId()));
            } else {
                model.addAttribute("transactions", transactionService.getAllTransactions());
            }
        } else {
            model.addAttribute("transactions", transactionService.getAllTransactions());
        }
        return "transaction/list";
    }

    @GetMapping("/new")
    public String showTransactionForm(Model model, @RequestParam(required = false) Long accountId, RedirectAttributes redirectAttributes) {
        try {
            Transaction transaction = new Transaction();
            transaction.setTransactionDate(LocalDateTime.now()); // Varsayılan tarih değeri
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            final User currentUser;
            
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                currentUser = userService.findByUsername(userDetails.getUsername());
            } else {
                currentUser = null;
            }
            
            if (accountId != null) {
                accountService.getAccountById(accountId).ifPresent(account -> {
                    // Kullanıcı sadece kendi hesabına işlem ekleyebilir
                    if (currentUser != null && account.getUser() != null && 
                        !account.getUser().getId().equals(currentUser.getId())) {
                        return;
                    }
                    transaction.setAccount(account);
                });
            } else if (currentUser != null) {
                // Kullanıcının hesaplarını getir
                List<Account> userAccounts = accountService.getAccountsByUserId(currentUser.getId());
                if (!userAccounts.isEmpty()) {
                    transaction.setAccount(userAccounts.get(0)); // İlk hesabı seç
                }
            }
            
            model.addAttribute("transaction", transaction);
            
            if (currentUser != null) {
                // Kullanıcı sadece kendi hesaplarını görebilir
                model.addAttribute("accounts", accountService.getAccountsByUserId(currentUser.getId()));
            } else {
                model.addAttribute("accounts", accountService.getAllAccounts());
            }
            
            return "transaction/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "İşlem formu yüklenirken bir hata oluştu: " + e.getMessage());
            return "redirect:/accounts";
        }
    }

    @PostMapping
    public String saveTransaction(@ModelAttribute Transaction transaction, 
                                 @RequestParam(name = "transactionDate", required = false) String transactionDateStr,
                                 @RequestParam(name = "account", required = false) Long accountId,
                                 RedirectAttributes redirectAttributes) {
        try {
            // Hesap kontrolü
            if (accountId != null) {
                accountService.getAccountById(accountId).ifPresent(transaction::setAccount);
            }
            
            // Tarih kontrolü
            if (transactionDateStr != null && !transactionDateStr.isEmpty()) {
                try {
                    LocalDate date = LocalDate.parse(transactionDateStr);
                    transaction.setTransactionDate(date.atStartOfDay());
                } catch (Exception e) {
                    transaction.setTransactionDate(LocalDateTime.now());
                }
            } else if (transaction.getTransactionDate() == null) {
                transaction.setTransactionDate(LocalDateTime.now());
            }
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                User user = userService.findByUsername(userDetails.getUsername());
                
                if (user != null) {
                    // Kullanıcı ile ilişkilendir
                    transaction.setUser(user);
                    
                    // Kullanıcı sadece kendi hesabına işlem ekleyebilir
                    if (transaction.getAccount() != null && transaction.getAccount().getUser() != null && 
                        !transaction.getAccount().getUser().getId().equals(user.getId())) {
                        redirectAttributes.addFlashAttribute("error", "Bu hesaba işlem ekleme yetkiniz yok!");
                        return "redirect:/transactions";
                    }
                }
            }
            
            transactionService.saveTransaction(transaction);
            redirectAttributes.addFlashAttribute("success", "İşlem başarıyla kaydedildi");
            return "redirect:/accounts/" + transaction.getAccount().getId();
        } catch (Exception e) {
            e.printStackTrace(); // Konsola hata detaylarını yazdır
            redirectAttributes.addFlashAttribute("error", "İşlem kaydedilirken bir hata oluştu: " + e.getMessage());
            return "redirect:/transactions/new";
        }
    }

    @GetMapping("/{id}")
    public String viewTransaction(@PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                // Kullanıcı sadece kendi işlemlerini görüntüleyebilir
                transactionService.getTransactionById(id).ifPresent(transaction -> {
                    if (transaction.getUser() != null && !transaction.getUser().getId().equals(user.getId())) {
                        return;
                    }
                    model.addAttribute("transaction", transaction);
                });
            } else {
                transactionService.getTransactionById(id)
                    .ifPresent(transaction -> model.addAttribute("transaction", transaction));
            }
        } else {
            transactionService.getTransactionById(id)
                .ifPresent(transaction -> model.addAttribute("transaction", transaction));
        }
        return "transaction/view";
    }

    @PostMapping("/{id}/delete")
    public String deleteTransaction(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long accountId = null;
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                // Kullanıcı sadece kendi işlemlerini silebilir
                Transaction transaction = transactionService.getTransactionById(id).orElse(null);
                if (transaction != null) {
                    if (transaction.getUser() != null && !transaction.getUser().getId().equals(user.getId())) {
                        redirectAttributes.addFlashAttribute("error", "Bu işlemi silme yetkiniz yok!");
                        return "redirect:/transactions";
                    }
                    accountId = transaction.getAccount().getId();
                }
            } else {
                accountId = transactionService.getTransactionById(id)
                    .map(t -> t.getAccount().getId())
                    .orElse(null);
            }
        } else {
            accountId = transactionService.getTransactionById(id)
                .map(t -> t.getAccount().getId())
                .orElse(null);
        }
            
        transactionService.deleteTransaction(id);
        redirectAttributes.addFlashAttribute("message", "Transaction deleted successfully!");
        
        return accountId != null ? "redirect:/accounts/" + accountId : "redirect:/transactions";
    }

    @GetMapping("/account/{accountId}")
    public String listAccountTransactions(@PathVariable Long accountId, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                // Kullanıcı sadece kendi hesabının işlemlerini görüntüleyebilir
                accountService.getAccountById(accountId).ifPresent(account -> {
                    if (account.getUser() != null && !account.getUser().getId().equals(user.getId())) {
                        return;
                    }
                    model.addAttribute("account", account);
                    model.addAttribute("transactions", transactionService.getTransactionsByAccountId(accountId));
                });
            } else {
                accountService.getAccountById(accountId).ifPresent(account -> {
                    model.addAttribute("account", account);
                    model.addAttribute("transactions", transactionService.getTransactionsByAccountId(accountId));
                });
            }
        } else {
            accountService.getAccountById(accountId).ifPresent(account -> {
                model.addAttribute("account", account);
                model.addAttribute("transactions", transactionService.getTransactionsByAccountId(accountId));
            });
        }
        return "transaction/list";
    }
    
    @GetMapping("/user")
    public String listUserTransactions(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                model.addAttribute("transactions", transactionService.getTransactionsByUserId(user.getId()));
                model.addAttribute("totalCredit", transactionService.getTotalCreditByUserId(user.getId()));
                model.addAttribute("totalDebit", transactionService.getTotalDebitByUserId(user.getId()));
            }
        }
        return "transaction/user_transactions";
    }
} 