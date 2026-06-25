package com.telemedclinic.config.seeder;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.telemedclinic.user.repository.DoctorRepository;
import com.telemedclinic.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DoctorSeeder {
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void seed() {
        // Doctor Default
        if (!userRepository.existsByEmail("doctor@klinikku.id")) {
            com.telemedclinic.user.entity.Doctor doctor = new com.telemedclinic.user.entity.Doctor(
                    "Dr. Ahmad Fauzi",
                    "doctor@klinikku.id",
                    passwordEncoder.encode("doctor123"),
                    "081299988877",
                    "Umum",
                    "SIP-2025-001");
            doctor.approveApplication();
            doctor.clearMustChangePassword();
            doctor.setBalance(2500000.0);
            doctor.setConsultationFee(25000.0);
            doctorRepository.save(doctor);
        } else {
            userRepository.findByEmail("doctor@klinikku.id").ifPresent(user -> {
                user.setName("Dr. Ahmad Fauzi"); // Sinkronkan nama jika sebelumnya berbeda
                userRepository.save(user);

                if (user instanceof com.telemedclinic.user.entity.Doctor doc) {
                    if (doc.getBalance() == null || doc.getBalance() == 0.0) {
                        doc.setBalance(2500000.0);
                        doc.setConsultationFee(250000.0);
                        doctorRepository.save(doc);
                    }
                }
            });
        }

        // Doctor Denna Wahyu
        if (!userRepository.existsByEmail("denna@klinikku.id")) {
            com.telemedclinic.user.entity.Doctor denna = new com.telemedclinic.user.entity.Doctor(
                    "dr. Denna Wahyu",
                    "denna@klinikku.id",
                    passwordEncoder.encode("denna123"),
                    "081288887777",
                    "Umum",
                    "SIP-2025-002");
            denna.approveApplication();
            denna.clearMustChangePassword();
            denna.setBalance(1000000.0);
            denna.setConsultationFee(15000.0);
            doctorRepository.save(denna);
        }
        log.info("Doctors berhasil disesuaikan");
    }
}
