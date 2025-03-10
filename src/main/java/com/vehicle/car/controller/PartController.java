package com.vehicle.car.controller;

import com.vehicle.car.model.Part;
import com.vehicle.car.model.User;
import com.vehicle.car.service.PartService;
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
@RequiredArgsConstructor
@RequestMapping("/parts")
public class PartController {
    private final PartService partService;
    private final UserService userService;

    @GetMapping
    public String listParts(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                // Kullanıcının kendi parçalarını göster
                model.addAttribute("parts", partService.getPartsByUserId(user.getId()));
            } else {
                model.addAttribute("parts", partService.getAllParts());
            }
        } else {
            model.addAttribute("parts", partService.getAllParts());
        }
        return "part/list";
    }

    @GetMapping("/new")
    public String showPartForm(Model model) {
        model.addAttribute("part", new Part());
        return "part/form";
    }

    @GetMapping("/{id}/edit")
    public String editPart(@PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                // Kullanıcının kendi parçasını düzenlemesine izin ver
                partService.getPartById(id).ifPresent(part -> {
                    if (part.getUser() != null && !part.getUser().getId().equals(user.getId())) {
                        return; // Başka kullanıcının parçasını düzenlemeye çalışıyor
                    }
                    model.addAttribute("part", part);
                });
            } else {
                partService.getPartById(id).ifPresent(part -> 
                    model.addAttribute("part", part));
            }
        } else {
            partService.getPartById(id).ifPresent(part -> 
                model.addAttribute("part", part));
        }
        return "part/form";
    }

    @PostMapping
    public String savePart(@ModelAttribute Part part, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                try {
                    // Kullanıcı ile ilişkilendir
                    part.setUser(user);
                    partService.savePartForUser(part, user.getId());
                    redirectAttributes.addFlashAttribute("success", "Parça başarıyla kaydedildi.");
                } catch (IllegalStateException e) {
                    // Aynı kodlu parça hatası
                    redirectAttributes.addFlashAttribute("error", e.getMessage());
                    return "redirect:/parts/new";
                } catch (Exception e) {
                    // Diğer hatalar
                    redirectAttributes.addFlashAttribute("error", "Parça kaydedilirken bir hata oluştu: " + e.getMessage());
                    return "redirect:/parts/new";
                }
            } else {
                try {
                    partService.savePart(part);
                    redirectAttributes.addFlashAttribute("success", "Parça başarıyla kaydedildi.");
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("error", "Parça kaydedilirken bir hata oluştu: " + e.getMessage());
                    return "redirect:/parts/new";
                }
            }
        } else {
            try {
                partService.savePart(part);
                redirectAttributes.addFlashAttribute("success", "Parça başarıyla kaydedildi.");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Parça kaydedilirken bir hata oluştu: " + e.getMessage());
                return "redirect:/parts/new";
            }
        }
        
        return "redirect:/parts";
    }

    @PostMapping("/{id}/toggle")
    public String togglePart(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                // Kullanıcının kendi parçasını değiştirmesine izin ver
                partService.getPartById(id).ifPresent(part -> {
                    if (part.getUser() != null && !part.getUser().getId().equals(user.getId())) {
                        return; // Başka kullanıcının parçasını değiştirmeye çalışıyor
                    }
                    partService.togglePartStatus(id);
                });
            } else {
                partService.togglePartStatus(id);
            }
        } else {
            partService.togglePartStatus(id);
        }
        return "redirect:/parts";
    }

    @PostMapping("/{id}/delete")
    public String deletePart(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                // Kullanıcının kendi parçasını silmesine izin ver
                partService.getPartById(id).ifPresent(part -> {
                    if (part.getUser() != null && !part.getUser().getId().equals(user.getId())) {
                        return; // Başka kullanıcının parçasını silmeye çalışıyor
                    }
                    partService.deletePart(id);
                });
            } else {
                partService.deletePart(id);
            }
        } else {
            partService.deletePart(id);
        }
        return "redirect:/parts";
    }

    @GetMapping("/{id}")
    public String viewPart(@PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                // Kullanıcının kendi parçasını görüntülemesine izin ver
                partService.getPartById(id).ifPresent(part -> {
                    if (part.getUser() != null && !part.getUser().getId().equals(user.getId())) {
                        return; // Başka kullanıcının parçasını görüntülemeye çalışıyor
                    }
                    model.addAttribute("part", part);
                    model.addAttribute("movements", partService.getPartMovementsByPartIdAndUserId(id, user.getId()));
                });
            } else {
                partService.getPartById(id).ifPresent(part -> {
                    model.addAttribute("part", part);
                    model.addAttribute("movements", partService.getPartMovements(id));
                });
            }
        } else {
            partService.getPartById(id).ifPresent(part -> {
                model.addAttribute("part", part);
                model.addAttribute("movements", partService.getPartMovements(id));
            });
        }
        return "part/view";
    }

    @PostMapping("/{id}/stock/add")
    public String addStock(
            @PathVariable Long id,
            @RequestParam Integer quantity,
            @RequestParam String reason,
            @RequestParam(required = false) String documentNo,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        try {
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                User user = userService.findByUsername(userDetails.getUsername());
                
                if (user != null) {
                    // Kullanıcının kendi parçasına stok eklemesine izin ver
                    partService.getPartById(id).ifPresent(part -> {
                        if (part.getUser() != null && !part.getUser().getId().equals(user.getId())) {
                            throw new IllegalStateException("Bu parça üzerinde işlem yapma yetkiniz yok");
                        }
                        partService.addStockForUser(id, quantity, reason, documentNo, notes, user.getId());
                    });
                } else {
                    partService.addStock(id, quantity, reason, documentNo, notes);
                }
            } else {
                partService.addStock(id, quantity, reason, documentNo, notes);
            }
            redirectAttributes.addFlashAttribute("success", "Stok başarıyla eklendi.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Stok eklenirken bir hata oluştu: " + e.getMessage());
        }
        
        return "redirect:/parts/" + id;
    }

    @PostMapping("/{id}/stock/remove")
    public String removeStock(
            @PathVariable Long id,
            @RequestParam Integer quantity,
            @RequestParam String reason,
            @RequestParam(required = false) String documentNo,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        try {
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                User user = userService.findByUsername(userDetails.getUsername());
                
                if (user != null) {
                    // Kullanıcının kendi parçasından stok çıkarmasına izin ver
                    partService.getPartById(id).ifPresent(part -> {
                        if (part.getUser() != null && !part.getUser().getId().equals(user.getId())) {
                            throw new IllegalStateException("Bu parça üzerinde işlem yapma yetkiniz yok");
                        }
                        partService.removeStockForUser(id, quantity, reason, documentNo, notes, user.getId());
                    });
                } else {
                    partService.removeStock(id, quantity, reason, documentNo, notes);
                }
            } else {
                partService.removeStock(id, quantity, reason, documentNo, notes);
            }
            redirectAttributes.addFlashAttribute("success", "Stok başarıyla çıkarıldı.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Stok çıkarılırken bir hata oluştu: " + e.getMessage());
        }
        
        return "redirect:/parts/" + id;
    }

    @GetMapping("/search")
    public String searchParts(@RequestParam String query, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                // Kullanıcının kendi parçalarını arama
                model.addAttribute("parts", partService.searchPartsByUserId(query, user.getId()));
            } else {
                model.addAttribute("parts", partService.searchParts(query));
            }
        } else {
            model.addAttribute("parts", partService.searchParts(query));
        }
        model.addAttribute("query", query);
        return "part/list";
    }

    @GetMapping("/category/{category}")
    public String getPartsByCategory(@PathVariable String category, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                // Kullanıcının kendi kategorisindeki parçaları göster
                model.addAttribute("parts", partService.getPartsByCategoryAndUserId(category, user.getId()));
            } else {
                model.addAttribute("parts", partService.getPartsByCategory(category));
            }
        } else {
            model.addAttribute("parts", partService.getPartsByCategory(category));
        }
        return "part/list";
    }

    @GetMapping("/supplier/{supplier}")
    public String getPartsBySupplier(@PathVariable String supplier, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                // Kullanıcının kendi tedarikçisindeki parçaları göster
                model.addAttribute("parts", partService.getPartsBySupplierAndUserId(supplier, user.getId()));
            } else {
                model.addAttribute("parts", partService.getPartsBySupplier(supplier));
            }
        } else {
            model.addAttribute("parts", partService.getPartsBySupplier(supplier));
        }
        return "part/list";
    }

    @GetMapping("/api/check-stock/{code}")
    @ResponseBody
    public Part checkStock(@PathVariable String code) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                // Kullanıcının kendi parçasının stok kontrolü
                return partService.getPartByCodeAndUserId(code, user.getId()).orElse(null);
            }
        }
        return partService.getPartByCode(code).orElse(null);
    }
} 