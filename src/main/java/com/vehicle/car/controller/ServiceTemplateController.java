package com.vehicle.car.controller;

import com.vehicle.car.model.ServiceTemplate;
import com.vehicle.car.model.User;
import com.vehicle.car.service.ServiceTemplateService;
import com.vehicle.car.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/templates")
public class ServiceTemplateController {
    private final ServiceTemplateService serviceTemplateService;
    private final UserService userService;

    @GetMapping
    public String listTemplates(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                // Kullanıcının kendi şablonlarını göster
                model.addAttribute("templates", serviceTemplateService.getTemplatesByUserId(user.getId()));
            } else {
                model.addAttribute("templates", serviceTemplateService.getAllTemplates());
            }
        } else {
            model.addAttribute("templates", serviceTemplateService.getAllTemplates());
        }
        return "template/list";
    }

    @GetMapping("/new")
    public String showTemplateForm(Model model) {
        model.addAttribute("template", new ServiceTemplate());
        return "template/form";
    }

    @GetMapping("/{id}/edit")
    public String editTemplate(@PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                // Kullanıcının kendi şablonunu düzenlemesine izin ver
                serviceTemplateService.getTemplateById(id).ifPresent(template -> {
                    if (template.getUser() != null && !template.getUser().getId().equals(user.getId())) {
                        return; // Başka kullanıcının şablonunu düzenlemeye çalışıyor
                    }
                    model.addAttribute("template", template);
                });
            } else {
                serviceTemplateService.getTemplateById(id).ifPresent(template -> 
                    model.addAttribute("template", template));
            }
        } else {
            serviceTemplateService.getTemplateById(id).ifPresent(template -> 
                model.addAttribute("template", template));
        }
        return "template/form";
    }

    @PostMapping
    public String saveTemplate(@ModelAttribute ServiceTemplate template) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                // Kullanıcı ile ilişkilendir
                template.setUser(user);
            }
        }
        
        serviceTemplateService.saveTemplate(template);
        return "redirect:/templates";
    }

    @PostMapping("/{id}/toggle")
    public String toggleTemplate(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                // Kullanıcının kendi şablonunu değiştirmesine izin ver
                serviceTemplateService.getTemplateById(id).ifPresent(template -> {
                    if (template.getUser() != null && !template.getUser().getId().equals(user.getId())) {
                        return; // Başka kullanıcının şablonunu değiştirmeye çalışıyor
                    }
                    serviceTemplateService.toggleTemplateStatus(id);
                });
            } else {
                serviceTemplateService.toggleTemplateStatus(id);
            }
        } else {
            serviceTemplateService.toggleTemplateStatus(id);
        }
        return "redirect:/templates";
    }

    @PostMapping("/{id}/delete")
    public String deleteTemplate(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                // Kullanıcının kendi şablonunu silmesine izin ver
                serviceTemplateService.getTemplateById(id).ifPresent(template -> {
                    if (template.getUser() != null && !template.getUser().getId().equals(user.getId())) {
                        return; // Başka kullanıcının şablonunu silmeye çalışıyor
                    }
                    serviceTemplateService.deleteTemplate(id);
                });
            } else {
                serviceTemplateService.deleteTemplate(id);
            }
        } else {
            serviceTemplateService.deleteTemplate(id);
        }
        return "redirect:/templates";
    }

    @GetMapping("/api/by-kilometrage")
    @ResponseBody
    public List<ServiceTemplate> getTemplatesByKilometrage(@RequestParam Integer kilometrage) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                // Kullanıcının kendi şablonlarını göster
                return serviceTemplateService.getTemplatesByKilometrageAndUserId(kilometrage, user.getId());
            }
        }
        return serviceTemplateService.getTemplatesByKilometrage(kilometrage);
    }

    @GetMapping("/api/by-type")
    @ResponseBody
    public List<ServiceTemplate> getTemplatesByType(@RequestParam String serviceType) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (user != null) {
                // Kullanıcının kendi şablonlarını göster
                return serviceTemplateService.getTemplatesByServiceTypeAndUserId(serviceType, user.getId());
            }
        }
        return serviceTemplateService.getTemplatesByServiceType(serviceType);
    }
} 