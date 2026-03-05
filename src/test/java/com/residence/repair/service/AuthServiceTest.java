package com.residence.repair.service;

import com.residence.repair.domain.entity.Tenant;
import com.residence.repair.domain.entity.User;
import com.residence.repair.domain.enums.UserRole;
import com.residence.repair.dto.request.LoginRequest;
import com.residence.repair.dto.request.RegisterRequest;
import com.residence.repair.dto.response.TokenResponse;
import com.residence.repair.dto.response.UserResponse;
import com.residence.repair.exception.ApiException;
import com.residence.repair.repository.UserRepository;
import com.residence.repair.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerTenant_shouldSaveTenantAndReturnUserResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("tenant@test.com");
        request.setPasswordHash("test1234");
        request.setNom("Test");
        request.setPrenom("Test");
        request.setResidenceName("Residence A");
        request.setRoomNumber("301");

        when(userRepository.existsByEmail("tenant@test.com")).thenReturn(false);
        when(passwordEncoder.encode("test1234")).thenReturn("hashed");
        when(userRepository.save(any(Tenant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = authService.registerTenant(request);

        ArgumentCaptor<Tenant> tenantCaptor = ArgumentCaptor.forClass(Tenant.class);
        verify(userRepository).save(tenantCaptor.capture());
        assertEquals("hashed", tenantCaptor.getValue().getPasswordHash());
        assertEquals("tenant@test.com", response.getEmail());
        assertEquals(UserRole.TENANT, response.getRole());
    }

    @Test
    void registerTenant_shouldThrowWhenEmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("exists@test.com");
        when(userRepository.existsByEmail("exists@test.com")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> authService.registerTenant(request));
        assertEquals("EMAIL_ALREADY_EXISTS", ex.getCode());
    }

    @Test
    void registerTenant_shouldThrowWhenPasswordIsWeak() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("tenant@test.com");
        request.setPasswordHash("abcdefg");
        request.setNom("Test");
        request.setPrenom("Test");
        request.setResidenceName("Residence A");
        request.setRoomNumber("301");

        when(userRepository.existsByEmail("tenant@test.com")).thenReturn(false);

        ApiException ex = assertThrows(ApiException.class, () -> authService.registerTenant(request));
        assertEquals("INVALID_PASSWORD_FORMAT", ex.getCode());
    }

    @Test
    void login_shouldReturnTokenWhenCredentialsValid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("tenant@test.com");
        request.setPasswordHash("test1234");

        Tenant user = new Tenant();
        user.setEmail("tenant@test.com");
        user.setPasswordHash("hashed");
        user.setRole(UserRole.TENANT);

        when(userRepository.findByEmail("tenant@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("test1234", "hashed")).thenReturn(true);
        when(jwtUtils.generateAccessToken("tenant@test.com", UserRole.TENANT)).thenReturn("jwt-token");

        TokenResponse response = authService.login(request);
        assertEquals("jwt-token", response.getAccessToken());
    }

    @Test
    void login_shouldThrowWhenPasswordInvalid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("tenant@test.com");
        request.setPasswordHash("wrong");

        User user = new Tenant();
        user.setEmail("tenant@test.com");
        user.setPasswordHash("hashed");

        when(userRepository.findByEmail("tenant@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        ApiException ex = assertThrows(ApiException.class, () -> authService.login(request));
        assertEquals("INVALID_CREDENTIALS", ex.getCode());
    }
}
