package com.vehicle.car.controller;

import com.vehicle.car.model.User;
import com.vehicle.car.model.Vehicle;
import com.vehicle.car.model.ServiceRecord;
import com.vehicle.car.service.TransactionService;
import com.vehicle.car.service.UserService;
import com.vehicle.car.service.VehicleService;
import com.vehicle.car.service.ServiceRecordService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;

@Controller
public class DashboardController {

    private final VehicleService vehicleService;
    private final UserService userService;
    private final TransactionService transactionService;
    private final ServiceRecordService serviceRecordService;

    public DashboardController(VehicleService vehicleService, 
                             UserService userService, 
                             TransactionService transactionService,
                             ServiceRecordService serviceRecordService) {
        this.vehicleService = vehicleService;
        this.userService = userService;
        this.transactionService = transactionService;
        this.serviceRecordService = serviceRecordService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Get current authenticated user
        User currentUser = getCurrentUser();
        
        if (currentUser != null) {
            // Get vehicles for the current user
            List<Vehicle> userVehicles = vehicleService.getVehiclesByUserId(currentUser.getId());
            
            // Calculate total vehicles
            int totalVehicles = userVehicles.size();
            
            // Calculate new vehicles added this month
            YearMonth currentMonth = YearMonth.now();
            LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
            
            int monthlyNewVehicles = (int) userVehicles.stream()
                    .filter(vehicle -> vehicle.getCreatedAt() != null && 
                                      vehicle.getCreatedAt().isAfter(startOfMonth))
                    .count();
            
            // Get most frequent service operations
            List<ServiceRecord> allServiceRecords = serviceRecordService.getServiceRecordsByUserId(currentUser.getId());
            Map<String, Long> serviceCounts = allServiceRecords.stream()
                .collect(Collectors.groupingBy(
                    ServiceRecord::getDescription,
                    Collectors.counting()
                ));

            // Sort by count and get top 5
            List<Map.Entry<String, Long>> topServices = serviceCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

            // Get top vehicles by service count
            List<Map<String, Object>> topVehicles = userVehicles.stream()
                .map(vehicle -> {
                    List<ServiceRecord> vehicleServices = serviceRecordService.getServiceRecordsByVehicleId(vehicle.getId());
                    BigDecimal totalSpent = vehicleServices.stream()
                        .map(ServiceRecord::getCost)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    Map<String, Object> vehicleInfo = new HashMap<>();
                    vehicleInfo.put("plate", vehicle.getPlate());
                    vehicleInfo.put("brand", vehicle.getBrand());
                    vehicleInfo.put("model", vehicle.getModel());
                    vehicleInfo.put("serviceCount", vehicleServices.size());
                    vehicleInfo.put("totalSpent", totalSpent);
                    return vehicleInfo;
                })
                .sorted((v1, v2) -> ((Integer)v2.get("serviceCount")).compareTo((Integer)v1.get("serviceCount")))
                .limit(5)
                .collect(Collectors.toList());

            // Calculate active services and completed services today
            int activeServices = (int) allServiceRecords.stream()
                .filter(record -> "Devam Ediyor".equals(record.getStatus()))
                .count();
            
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0);
            
            int completedServicesToday = (int) allServiceRecords.stream()
                .filter(record -> "Tamamlandı".equals(record.getStatus()))
                .filter(record -> record.getServiceDate() != null && 
                                record.getServiceDate().isAfter(startOfDay))
                .count();

            // Prepare data for chart
            List<String> serviceLabels = topServices.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
            
            List<Long> serviceData = topServices.stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

            // Calculate monthly income
            double monthlyIncome = allServiceRecords.stream()
                .filter(s -> s.getServiceDate() != null && s.getServiceDate().isAfter(startOfMonth))
                .mapToDouble(s -> s.getCost() != null ? s.getCost().doubleValue() : 0.0)
                .sum();
            
            // Calculate previous month's income for comparison
            LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);
            double lastMonthIncome = allServiceRecords.stream()
                .filter(s -> s.getServiceDate() != null 
                        && s.getServiceDate().isAfter(startOfLastMonth) 
                        && s.getServiceDate().isBefore(startOfMonth))
                .mapToDouble(s -> s.getCost() != null ? s.getCost().doubleValue() : 0.0)
                .sum();

            // Calculate percentage change
            double incomeChange = lastMonthIncome > 0 
                    ? ((monthlyIncome - lastMonthIncome) / lastMonthIncome) * 100 
                    : 100;

            // Prepare monthly income chart data
            List<String> monthlyIncomeLabels = new ArrayList<>();
            List<Double> monthlyIncomeData = new ArrayList<>();

            DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM");
            for (int i = 5; i >= 0; i--) {
                LocalDateTime month = LocalDateTime.now().minusMonths(i);
                LocalDateTime startOfTargetMonth = month.withDayOfMonth(1).withHour(0).withMinute(0);
                LocalDateTime startOfNextMonth = startOfTargetMonth.plusMonths(1);
                
                double income = allServiceRecords.stream()
                        .filter(s -> s.getServiceDate() != null 
                                && s.getServiceDate().isAfter(startOfTargetMonth)
                                && s.getServiceDate().isBefore(startOfNextMonth))
                        .mapToDouble(s -> s.getCost() != null ? s.getCost().doubleValue() : 0.0)
                        .sum();
                
                monthlyIncomeLabels.add(month.format(monthFormatter));
                monthlyIncomeData.add(income);
            }
            
            // Add data to model
            model.addAttribute("totalVehicles", totalVehicles);
            model.addAttribute("monthlyNewVehicles", monthlyNewVehicles);
            model.addAttribute("monthlyIncome", monthlyIncome);
            model.addAttribute("monthlyIncomeChange", Math.round(incomeChange));
            model.addAttribute("popularServicesLabels", serviceLabels);
            model.addAttribute("popularServicesData", serviceData);
            model.addAttribute("monthlyIncomeLabels", monthlyIncomeLabels);
            model.addAttribute("monthlyIncomeData", monthlyIncomeData);
            model.addAttribute("topVehicles", topVehicles);
            model.addAttribute("activeServices", activeServices);
            model.addAttribute("completedServicesToday", completedServicesToday);
        } else {
            // Default values if user is not authenticated
            model.addAttribute("totalVehicles", 0);
            model.addAttribute("monthlyNewVehicles", 0);
            model.addAttribute("monthlyIncome", BigDecimal.ZERO);
            model.addAttribute("monthlyIncomeChange", 0);
            model.addAttribute("popularServicesLabels", List.of());
            model.addAttribute("popularServicesData", List.of());
            model.addAttribute("topVehicles", List.of());
            model.addAttribute("activeServices", 0);
            model.addAttribute("completedServicesToday", 0);
        }
        
        return "dashboard/index";
    }
    
    // Helper method to get current user
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userService.findByUsername(userDetails.getUsername());
        }
        return null;
    }
} 