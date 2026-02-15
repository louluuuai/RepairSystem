package com.residence.repair.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Register {
    @NotBlank
    private String email;
    @NotBlank
    private String passwordHash;

    private String nom;
    private String prenom;
    private String employeeId;
}
