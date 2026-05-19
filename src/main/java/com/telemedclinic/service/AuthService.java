package com.telemedclinic.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.telemedclinic.dto.AuthResponse;
import com.telemedclinic.dto.CreatePharmacistAccountRequest;
import com.telemedclinic.dto.CustomerRegisterRequest;
import com.telemedclinic.dto.DoctorRegisterRequest;
import com.telemedclinic.dto.LoginRequest;
import com.telemedclinic.model.Admin;
import com.telemedclinic.model.Customer;
import com.telemedclinic.model.Doctor;
import com.telemedclinic.model.Pharmacist;
import com.telemedclinic.model.Pharmacy;
import com.telemedclinic.model.Role;
import com.telemedclinic.model.User;
import com.telemedclinic.repository.PharmacyRepository;
import com.telemedclinic.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PharmacyRepository pharmacyRepository,
            PasswordEncoder passwordEncoder
    ) {

        this.userRepository = userRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse registerCustomer(CustomerRegisterRequest request) {
        ensureEmailIsAvailable(request.getEmail());

        Customer customer = new Customer(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getPhoneNumber(),
                request.getAddress()
        );

        return toAuthResponse(userRepository.save(customer));
    }

    public AuthResponse registerDoctor(DoctorRegisterRequest request) {
        ensureEmailIsAvailable(request.getEmail());

        Doctor doctor = new Doctor(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getPhoneNumber(),
                request.getSpecialization(),
                request.getLicenseNumber()
        );

        return toAuthResponse(userRepository.save(doctor));
    }

    public AuthResponse createPharmacistAccount(CreatePharmacistAccountRequest request) {
        ensureEmailIsAvailable(request.getEmail());

        Pharmacy pharmacy = pharmacyRepository.findById(request.getPharmacyId())
                .orElseThrow(() -> new IllegalArgumentException("Pharmacy not found."));

        if (!pharmacy.isActive()) {
            throw new IllegalStateException("Pharmacy is not active.");
        }

        Pharmacist pharmacist = new Pharmacist(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getPhoneNumber(),
                request.getLicenseNumber(),
                pharmacy
        );

        return toAuthResponse(userRepository.save(pharmacist));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (!user.matchesPassword(request.getPassword(), passwordEncoder)) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        return toAuthResponse(user);
    }

    private void ensureEmailIsAvailable(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered.");
        }
    }

    private AuthResponse toAuthResponse(User user) {
        return new AuthResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                resolveRole(user)
        );
    }

    private Role resolveRole(User user) {
        if (user instanceof Customer) {
            return Role.ROLE_CUSTOMER;
        }

        if (user instanceof Doctor) {
            return Role.ROLE_DOCTOR;
        }

        if (user instanceof Pharmacist) {
            return Role.ROLE_PHARMACIST;
        }

        if (user instanceof Admin) {
            return Role.ROLE_ADMIN;
        }

        throw new IllegalArgumentException("Unknown user role.");
    }
}
