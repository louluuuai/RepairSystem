/**
 * @file AuthService.java
 * @author
 * @date 2026-02-21
 * @version 1.0
 */
package com.residence.repair.service;

import com.residence.repair.domain.entity.Admin;
import com.residence.repair.domain.entity.Tenant;
import com.residence.repair.domain.entity.User;
import com.residence.repair.domain.enums.UserRole;
import com.residence.repair.dto.request.LoginRequest;
import com.residence.repair.dto.response.TokenResponse;
import com.residence.repair.dto.response.UserResponse;
import com.residence.repair.dto.request.RegisterRequest;
import com.residence.repair.repository.UserRepository;
import com.residence.repair.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service d'authentification:
 * - inscription (tenant)
 * - connexion (login)
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    /**
     * Inscription des locataires (TENANT uniquement).
     */
    @Transactional
    public UserResponse registerTenant(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Tenant tenant = new Tenant();
        tenant.setEmail(request.getEmail());
        // Hashage du mot de passe
        tenant.setPasswordHash(passwordEncoder.encode(request.getPasswordHash()));

        tenant.setNom(request.getNom());
        tenant.setPrenom(request.getPrenom());
        tenant.setResidenceName(request.getResidenceName());
        tenant.setRoomNumber(request.getRoomNumber());

        Tenant savedTenant = userRepository.save(tenant);
        return UserResponse.builder()
                .email(savedTenant.getEmail())
                .role(UserRole.TENANT)
                .nom(savedTenant.getNom())
                .prenom(savedTenant.getPrenom())
                .build();
    }
    /**
     * Authentification et génération de Access Token.
     */
    public TokenResponse login(LoginRequest dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email"));

        if (!passwordEncoder.matches(dto.getPasswordHash(), user.getPasswordHash())) {
            throw new RuntimeException("The password doesn't match");
        }
        String accessToken = jwtUtils.generateAccessToken(user.getEmail(), user.getRole());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .build();
    }
}