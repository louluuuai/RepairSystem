package com.residence.repair.dto.response;

import com.residence.repair.domain.enums.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private String email;
    private UserRole role;
    private String nom;
    private String prenom;
}
