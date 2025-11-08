package com.TenX.Automobile.service;

import com.TenX.Automobile.model.entity.Admin;
import com.TenX.Automobile.model.enums.Role;
import com.TenX.Automobile.repository.AdminRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminInitializationService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.first-name}")
    private String adminFirstName;

    @Value("${app.admin.last-name}")
    private String adminLastName;

    @Value("${app.admin.phone}")
    private String adminPhone;

    @PostConstruct
    @Transactional
    public void initializeDefaultAdmin() {
        try {
            log.info("Checking for default admin account...");

            // Check if admin already exists
            if (adminRepository.existsByEmail(adminEmail)) {
                log.info("Default admin account already exists with email: {}", adminEmail);
                return;
            }

            // Create default admin account
            Admin admin = Admin.builder()
                    .adminId("ADM0001")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .firstName(adminFirstName)
                    .lastName(adminLastName)
                    .phoneNumber(adminPhone)
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();

            // Add ADMIN role
            admin.addRole(Role.ADMIN);

            // Save admin
            Admin savedAdmin = adminRepository.save(admin);

            log.info("✅ Default admin account created successfully!");
            log.info("   Admin ID: {}", savedAdmin.getAdminId());
            log.info("   Email: {}", savedAdmin.getEmail());
            log.info("   Name: {} {}", savedAdmin.getFirstName(), savedAdmin.getLastName());
            log.warn("⚠️  IMPORTANT: Please change the default admin password after first login!");

        } catch (Exception e) {
            log.error("❌ Failed to create default admin account", e);
        }
    }
}
