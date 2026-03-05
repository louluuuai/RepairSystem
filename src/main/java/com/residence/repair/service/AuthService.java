/**
 * @file AuthService.java
 * @author Xuelu AI
 */
package com.residence.repair.service;

import com.residence.repair.domain.entity.Tenant;
import com.residence.repair.domain.entity.User;
import com.residence.repair.domain.enums.UserRole;
import com.residence.repair.dto.request.LoginRequest;
import com.residence.repair.dto.response.TokenResponse;
import com.residence.repair.dto.response.UserResponse;
import com.residence.repair.dto.request.RegisterRequest;
import com.residence.repair.exception.ApiException;
import com.residence.repair.repository.UserRepository;
import com.residence.repair.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service d'authentification:
 * - inscription (tenant)
 * - connexion (login)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String PASSWORD_POLICY_REGEX = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    /**
     * Inscription des locataires (TENANT uniquement).
     */
    @Transactional
    public UserResponse registerTenant(RegisterRequest request) {
        log.info("New registration request for email: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException("EMAIL_ALREADY_EXISTS", "Email already exists", HttpStatus.CONFLICT);
        }
        if (!request.getPasswordHash().matches(PASSWORD_POLICY_REGEX)) {
            throw new ApiException(
                    "INVALID_PASSWORD_FORMAT",
                    "Password must be at least 8 characters and contain letters and numbers",
                    HttpStatus.BAD_REQUEST
            );
        }

        Tenant tenant = new Tenant();
        tenant.setEmail(request.getEmail());
        tenant.setRole(UserRole.TENANT);
        // Hashage du mot de passe
        tenant.setPasswordHash(passwordEncoder.encode(request.getPasswordHash()));

        tenant.setNom(request.getNom());
        tenant.setPrenom(request.getPrenom());
        tenant.setResidenceName(request.getResidenceName());
        tenant.setRoomNumber(request.getRoomNumber());

        Tenant savedTenant = userRepository.save(tenant);
        log.info("Registration successful for user: {}", savedTenant.getEmail());
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
        log.info("Login attempt for email: {}", dto.getEmail());

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ApiException("INVALID_CREDENTIALS", "Invalid email or password", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(dto.getPasswordHash(), user.getPasswordHash())) {
            throw new ApiException("INVALID_CREDENTIALS", "Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        String accessToken = jwtUtils.generateAccessToken(user.getEmail(), user.getRole());
        log.info("Login attempt for email: {}", dto.getEmail());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .build();
    }
}
