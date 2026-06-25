package com.telemedclinic.config.seeder;

import org.springframework.stereotype.Component;

import com.telemedclinic.pharmacy.internal.entity.PharmacyOwner;
import com.telemedclinic.pharmacy.internal.repository.PharmacyRepository;
import com.telemedclinic.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PharmacySeeder {
    private final PharmacyRepository pharmacyRepository;
    private final UserRepository userRepository;

    public void seed() {
        PharmacyOwner owner1 = (PharmacyOwner) userRepository.findByEmail("owner@klinikku.id").orElse(null);
        PharmacyOwner owner2 = (PharmacyOwner) userRepository.findByEmail("renisa@klinikku.id").orElse(null);

        if (owner1 == null || owner2 == null) {
            log.warn("Owners not found, skipping pharmacy seeding");
            return;
        }

        // Apotek 1: Dekat Alun-alun Purwokerto (Owner: owner@klinikku.id)
        if (!pharmacyRepository.existsByLegalDocumentNumber("SIA-PWT-001")) {
            com.telemedclinic.pharmacy.internal.entity.Pharmacy pharmacy1 = new com.telemedclinic.pharmacy.internal.entity.Pharmacy(
                    "Apotek Medika Farma (Alun-Alun)",
                    "Jl. Masjid No. 1, Purwokerto",
                    "0281-111111",
                    "SIA-PWT-001",
                    -7.424500,
                    109.231000
            );
            pharmacy1.setOwner(owner1);
            pharmacyRepository.save(pharmacy1);
        }

        // Apotek 2: Dekat Unsoed (Owner: renisa@klinikku.id)
        if (!pharmacyRepository.existsByLegalDocumentNumber("SIA-PWT-002")) {
            com.telemedclinic.pharmacy.internal.entity.Pharmacy pharmacy2 = new com.telemedclinic.pharmacy.internal.entity.Pharmacy(
                    "Apotek Sehat Bersama (Unsoed)",
                    "Jl. HR Bunyamin, Purwokerto",
                    "0281-222222",
                    "SIA-PWT-002",
                    -7.408000,
                    109.251000
            );
            pharmacy2.setOwner(owner2);
            pharmacyRepository.save(pharmacy2);
        }

        // Apotek 3: Daerah Sokaraja (Owner: renisa@klinikku.id)
        if (!pharmacyRepository.existsByLegalDocumentNumber("SIA-PWT-003")) {
            com.telemedclinic.pharmacy.internal.entity.Pharmacy pharmacy3 = new com.telemedclinic.pharmacy.internal.entity.Pharmacy(
                    "Apotek Banyu Urip (Sokaraja)",
                    "Jl. Jend. Sudirman Sokaraja",
                    "0281-333333",
                    "SIA-PWT-003",
                    -7.452000,
                    109.288000
            );
            pharmacy3.setOwner(owner2);
            pharmacyRepository.save(pharmacy3);
        }
        log.info("Pharmacies berhasil disesuaikan");
    }
}
