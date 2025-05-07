package com.vehicle.car.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetRequest {
    @NotBlank
    private String username;
    
    @NotBlank
    private String email;
} 