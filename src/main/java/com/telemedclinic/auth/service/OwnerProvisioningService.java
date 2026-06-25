package com.telemedclinic.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.telemedclinic.pharmacy.internal.entity.PharmacyOwner;
import com.telemedclinic.user.dto.CreateOwnerRequest;
import com.telemedclinic.user.repository.UserRepository;

@Service
public class OwnerProvisioningService {

    private static final Logger logger = LoggerFactory.getLogger(OwnerProvisioningService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public OwnerProvisioningService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public OwnerProvisioningResult provisionOwner(CreateOwnerRequest request) {
        String tempPassword = PasswordGenerator.generate();
        PharmacyOwner owner = createAndSaveOwner(request, tempPassword);

        EmailResult emailResult = emailService.sendOwnerCredentials(
                request.getEmail(),
                request.getName(),
                tempPassword
        );

        if (emailResult.isSuccess()) {
            return OwnerProvisioningResult.success(owner);
        }

        logger.warn(
                "Owner account was created, but credential email failed for {}. Error: {}",
                request.getEmail(),
                emailResult.getErrorMessage()
        );

        return OwnerProvisioningResult.emailFailed(
                owner,
                emailResult.getErrorMessage()
        );
    }

    @Transactional
    protected PharmacyOwner createAndSaveOwner(
            CreateOwnerRequest request,
            String tempPassword
    ) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        PharmacyOwner owner = new PharmacyOwner(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(tempPassword),
                request.getPhoneNumber(),
                null
        );

        return userRepository.save(owner);
    }
}
