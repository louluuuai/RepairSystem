package com.residence.repair.dto.request;

import com.residence.repair.domain.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String email;
    @NotBlank
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
