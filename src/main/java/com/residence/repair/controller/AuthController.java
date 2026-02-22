package com.residence.repair.controller;

import com.residence.repair.dto.request.LoginRequest;
import com.residence.repair.dto.request.RegisterRequest;
import com.residence.repair.dto.response.TokenResponse;
import com.residence.repair.dto.response.UserResponse;
import com.residence.repair.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur pour l'authentification et l'inscription.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Inscription publique (Uniquement Locataire).
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.registerTenant(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Connexion et obtention du token.
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}