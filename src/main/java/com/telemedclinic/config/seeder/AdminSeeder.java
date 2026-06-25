package com.telemedclinic.config.seeder;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.telemedclinic.user.entity.Admin;
import com.telemedclinic.user.repository.AdminRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder {
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public void seed() {
        if (!adminRepository.existsByEmail("admin@klinikku.id")) {
            Admin admin = new Admin(
                    "Administrator",
                    "admin@klinikku.id",
                    passwordEncoder.encode("admin123"),
                    "081200000000",
                    "EMP-0001"
            );
            adminRepository.save(admin);
            log.info("Admin default berhasil dibuat");
        }
    }
}
