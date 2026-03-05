package com.residence.repair.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String email;
    @NotBlank
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
            message = "Password must be at least 8 characters and contain letters and numbers"
    )
    private String passwordHash;
    @NotBlank
    private String nom;
    @NotBlank
    private String prenom;
    @NotBlank
    private String residenceName;
    @NotBlank
    private String roomNumber;
}
