package com.residence.repair.dto.reponse;

import com.residence.repair.domain.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class UserReponse {
    private Integer id;
    private String email;
    private UserRole role;
    private String nom;
    private String prenom;

    private String employeeId;
}
