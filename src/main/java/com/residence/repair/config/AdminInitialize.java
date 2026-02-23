package com.residence.repair.config;

import com.residence.repair.domain.entity.Admin;
import com.residence.repair.domain.enums.UserRole;
import com.residence.repair.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initialisation des données au démarrage avec journalisation.
 */
@Slf4j // Génère automatiquement la variable 'log'
@Component
@RequiredArgsConstructor
public class AdminInitialize implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        String adminEmail = "admin@residence.com";

        if (userRepository.existsByEmail(adminEmail)) {
            log.info("System initialization : Admin user '{}' already exists.", adminEmail);
            return;
        }

        try {
            Admin admin = new Admin();
            admin.setEmail(adminEmail);
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setRole(UserRole.ADMIN);
            admin.setEmployeeId("ADMIN-001");

            userRepository.save(admin);
            log.info("System initialization : Admin account successfully created for '{}'.", adminEmail);
        } catch (Exception e) {
            log.error("System initialization failed : Error creating admin account. {}", e.getMessage());
        }
    }
}
