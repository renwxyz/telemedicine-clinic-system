package com.telemedclinic.config.seeder;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.telemedclinic.pharmacy.internal.entity.PharmacyOwner;
import com.telemedclinic.pharmacy.internal.repository.PharmacyOwnerRepository;
import com.telemedclinic.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PharmacyOwnerSeeder {
    private final PharmacyOwnerRepository pharmacyOwnerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void seed() {
        // Owner Default
        if (!userRepository.existsByEmail("owner@klinikku.id")) {
            PharmacyOwner owner = new PharmacyOwner(
                    "Budi Santoso",
                    "owner@klinikku.id",
                    passwordEncoder.encode("owner123"),
                    "081255556666",
                    "3170000000000001"
            );
            pharmacyOwnerRepository.save(owner);
        }

        // Owner Renisa
        if (!userRepository.existsByEmail("renisa@klinikku.id")) {
            PharmacyOwner renisa = new PharmacyOwner(
                    "Renisa",
                    "renisa@klinikku.id",
                    passwordEncoder.encode("renisa123"),
                    "081266667777",
                    "3170000000000002"
            );
            pharmacyOwnerRepository.save(renisa);
        }
        log.info("Pharmacy Owners berhasil disesuaikan");
    }
}
