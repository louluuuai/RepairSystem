package com.residence.repair.config;

import com.residence.repair.domain.entity.Admin;
import com.residence.repair.domain.enums.UserRole;
import com.residence.repair.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initialisation des données au démarrage.
 */
@Component
@RequiredArgsConstructor
public class AdminInitialize implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        String adminEmail = "admin@residence.com";

        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }

        Admin admin = new Admin();
        admin.setEmail(adminEmail);
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setRole(UserRole.ADMIN);
        admin.setEmployeeId("ADMIN-001");

        userRepository.save(admin);
    }
}
