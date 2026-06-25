package com.telemedclinic.config.seeder;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.telemedclinic.pharmacy.internal.entity.Pharmacist;
import com.telemedclinic.pharmacy.internal.repository.PharmacyRepository;
import com.telemedclinic.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PharmacistSeeder {
    private final UserRepository userRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PasswordEncoder passwordEncoder;

    public void seed() {
        com.telemedclinic.pharmacy.internal.entity.Pharmacy pharmacy1 = pharmacyRepository.findByLegalDocumentNumber("SIA-PWT-001").orElse(null);
        com.telemedclinic.pharmacy.internal.entity.Pharmacy pharmacy2 = pharmacyRepository.findByLegalDocumentNumber("SIA-PWT-002").orElse(null);
        com.telemedclinic.pharmacy.internal.entity.Pharmacy pharmacy3 = pharmacyRepository.findByLegalDocumentNumber("SIA-PWT-003").orElse(null);

        if (pharmacy1 == null || pharmacy2 == null || pharmacy3 == null) {
            log.warn("Pharmacies not found, skipping pharmacist seeding");
            return;
        }

        // Pharmacist 1 (Apotek 1)
        if (!userRepository.existsByEmail("pharmacist@klinikku.id")) {
            Pharmacist pharmacist = new Pharmacist(
                    "Dian Salsabila, S.Farm., Apt.",
                    "pharmacist@klinikku.id",
                    passwordEncoder.encode("pharmacist123"),
                    "081244443333",
                    "SIK-PWT-001",
                    pharmacy1
            );
            pharmacist.clearMustChangePassword();
            userRepository.save(pharmacist);
        }

        // Pharmacist Rendhi (Apotek 2)
        if (!userRepository.existsByEmail("rendhi@klinikku.id")) {
            Pharmacist rendhi = new Pharmacist(
                    "Rendhi, S.Farm., Apt.",
                    "rendhi@klinikku.id",
                    passwordEncoder.encode("rendhi123"),
                    "081255554444",
                    "SIK-PWT-002",
                    pharmacy2
            );
            rendhi.clearMustChangePassword();
            userRepository.save(rendhi);
        }

        // Pharmacist 3 (Apotek 3)
        if (!userRepository.existsByEmail("pharmacist3@klinikku.id")) {
            Pharmacist pharmacist3 = new Pharmacist(
                    "Citra Kirana, S.Farm., Apt.",
                    "pharmacist3@klinikku.id",
                    passwordEncoder.encode("pharmacist123"),
                    "081266665555",
                    "SIK-PWT-003",
                    pharmacy3
            );
            pharmacist3.clearMustChangePassword();
            userRepository.save(pharmacist3);
        }
        log.info("Pharmacists berhasil disesuaikan");
    }
}
