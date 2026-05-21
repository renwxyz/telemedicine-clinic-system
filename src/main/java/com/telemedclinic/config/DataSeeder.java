package com.telemedclinic.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.telemedclinic.model.Admin;
import com.telemedclinic.repository.AdminRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private static final String DEFAULT_ADMIN_NAME = "Administrator";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@klinikku.id";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    private static final String DEFAULT_ADMIN_PHONE_NUMBER = "081200000000";
    private static final String DEFAULT_ADMIN_EMPLOYEE_NUMBER = "EMP-0001";

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminRepository.existsByEmail(DEFAULT_ADMIN_EMAIL)) {
            log.info("Admin default sudah ada, skip seeding");
            return;
        }

        Admin admin = new Admin(
                DEFAULT_ADMIN_NAME,
                DEFAULT_ADMIN_EMAIL,
                passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD),
                DEFAULT_ADMIN_PHONE_NUMBER,
                DEFAULT_ADMIN_EMPLOYEE_NUMBER
        );

        adminRepository.save(admin);
        log.info("Admin default berhasil dibuat");
    }
}
