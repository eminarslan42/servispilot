package com.vehicle.car.controller;

import com.vehicle.car.model.ServiceReminder;
import com.vehicle.car.service.ServiceReminderService;
import com.vehicle.car.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reminders")
public class ServiceReminderController {
    private final ServiceReminderService serviceReminderService;
    private final VehicleService vehicleService;

    @GetMapping
    public String listReminders(Model model) {
        model.addAttribute("reminders", serviceReminderService.getActiveReminders());
        return "reminder/list";
    }

    @GetMapping("/new")
    public String showReminderForm(@RequestParam(required = false) Long vehicleId, Model model) {
        ServiceReminder reminder = new ServiceReminder();
        if (vehicleId != null) {
            vehicleService.getVehicleById(vehicleId).ifPresent(reminder::setVehicle);
        }
        model.addAttribute("reminder", reminder);
        model.addAttribute("vehicles", vehicleService.getAllVehicles());
        return "reminder/form";
    }

    @PostMapping
    public String saveReminder(@ModelAttribute ServiceReminder reminder) {
        reminder.setStatus("Aktif");
        serviceReminderService.saveReminder(reminder);
        return "redirect:/vehicles/" + reminder.getVehicle().getId();
    }

    @PostMapping("/{id}/complete")
    public String completeReminder(@PathVariable Long id) {
        serviceReminderService.getReminderById(id).ifPresent(reminder -> {
            reminder.setStatus("Tamamlandı");
            serviceReminderService.saveReminder(reminder);
        });
        return "redirect:/reminders";
    }

    @PostMapping("/{id}/cancel")
    public String cancelReminder(@PathVariable Long id) {
        serviceReminderService.getReminderById(id).ifPresent(reminder -> {
            reminder.setStatus("İptal");
            serviceReminderService.saveReminder(reminder);
        });
        return "redirect:/reminders";
    }

    @PostMapping("/{id}/delete")
    public String deleteReminder(@PathVariable Long id) {
        serviceReminderService.deleteReminder(id);
        return "redirect:/reminders";
    }
} 