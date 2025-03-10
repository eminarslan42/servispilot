package com.vehicle.car.payload.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import com.vehicle.car.model.Role;

@Data
public class SignupRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;
 
    @NotBlank
    @Size(max = 50)
    @Email
    private String email;
    
    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    private Role role;
} 