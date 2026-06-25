package com.telemedclinic.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.telemedclinic.pharmacy.internal.entity.Pharmacy;
import com.telemedclinic.pharmacy.internal.repository.PharmacyRepository;
import com.telemedclinic.user.dto.CreatePharmacistRequest;
import com.telemedclinic.pharmacy.internal.entity.Pharmacist;
import com.telemedclinic.user.repository.UserRepository;

@Service
public class PharmacistProvisioningService {

    private static final Logger logger = LoggerFactory.getLogger(PharmacistProvisioningService.class);

    private final UserRepository userRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PharmacistProvisioningService(
            UserRepository userRepository,
            PharmacyRepository pharmacyRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {

        this.userRepository = userRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public PharmacistProvisioningResult provisionPharmacist(CreatePharmacistRequest request) {
        String tempPassword = PasswordGenerator.generate();
        Pharmacist pharmacist = createAndSavePharmacist(request, tempPassword);

        EmailResult emailResult = emailService.sendPharmacistCredentials(
                request.getEmail(),
                request.getName(),
                tempPassword
        );

        if (emailResult.isSuccess()) {
            pharmacist.markCredentialSent();
            userRepository.save(pharmacist);
            return PharmacistProvisioningResult.success(pharmacist);
        }

        logger.warn(
                "Pharmacist account was created, but credential email failed for {}. Error: {}",
                request.getEmail(),
                emailResult.getErrorMessage()
        );

        return PharmacistProvisioningResult.emailFailed(
                pharmacist,
                emailResult.getErrorMessage()
        );
    }

    @Transactional
    private Pharmacist createAndSavePharmacist(
            CreatePharmacistRequest request,
            String tempPassword
    ) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        Pharmacy pharmacy = pharmacyRepository.findById(request.getPharmacyId())
                .orElseThrow(() -> new IllegalArgumentException("Pharmacy not found."));

        if (!pharmacy.isApprovedPartner() || !pharmacy.isActive()) {
            throw new IllegalStateException("Pharmacy is not approved and active.");
        }

        Pharmacist pharmacist = new Pharmacist(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(tempPassword),
                request.getPhoneNumber(),
                request.getLicenseNumber(),
                pharmacy
        );

        return userRepository.save(pharmacist);
    }
}
