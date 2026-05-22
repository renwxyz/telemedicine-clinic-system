package com.telemedclinic.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.telemedclinic.auth.dto.AuthResponse;
import com.telemedclinic.user.dto.CreatePharmacistAccountRequest;
import com.telemedclinic.user.dto.CustomerRegisterRequest;
import com.telemedclinic.user.dto.DoctorRegisterRequest;
import com.telemedclinic.auth.dto.LoginRequest;
import com.telemedclinic.user.entity.Customer;
import com.telemedclinic.user.entity.Doctor;
import com.telemedclinic.user.entity.Pharmacist;
import com.telemedclinic.pharmacy.entity.Pharmacy;
import com.telemedclinic.user.entity.User;
import com.telemedclinic.user.repository.DoctorRepository;
import com.telemedclinic.pharmacy.repository.PharmacyRepository;
import com.telemedclinic.user.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            DoctorRepository doctorRepository,
            PharmacyRepository pharmacyRepository,
            PasswordEncoder passwordEncoder
    ) {

        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Mendaftarkan customer baru dengan password yang sudah di-hash.
    @Transactional
    public AuthResponse registerCustomer(CustomerRegisterRequest request) {
        ensureEmailIsAvailable(request.getEmail());

        Customer customer = new Customer(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getPhoneNumber(),
                request.getAddress(),
                request.getGender(),
                request.getBirthDate(),
                request.getHeight(),
                request.getWeight()
        );

        return toAuthResponse(userRepository.save(customer));
    }

    // Membuat akun doctor baru dengan status aplikasi default dari domain model.
    @Transactional
    public Doctor registerDoctor(DoctorRegisterRequest request) {
        ensureEmailIsAvailable(request.getEmail());

        Doctor doctor = new Doctor(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getPhoneNumber(),
                request.getSpecialization(),
                request.getLicenseNumber()
        );

        return userRepository.save(doctor);
    }

    // Menyimpan perubahan data doctor.
    @Transactional
    public Doctor saveDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    // Menyetujui aplikasi doctor agar dapat digunakan sebagai partner.
    @Transactional
    public Doctor approveDoctor(Long doctorId) {
        Doctor doctor = findDoctorById(doctorId);
        doctor.approveApplication();
        return doctorRepository.save(doctor);
    }

    // Menolak aplikasi doctor agar tidak dapat digunakan sebagai partner.
    @Transactional
    public Doctor declineDoctor(Long doctorId) {
        Doctor doctor = findDoctorById(doctorId);
        doctor.declineApplication();
        return doctorRepository.save(doctor);
    }

    // Membuat akun pharmacist untuk pharmacy yang sudah approved dan aktif.
    @Transactional
    public AuthResponse createPharmacistAccount(CreatePharmacistAccountRequest request) {
        ensureEmailIsAvailable(request.getEmail());

        Pharmacy pharmacy = pharmacyRepository.findById(request.getPharmacyId())
                .orElseThrow(() -> new IllegalArgumentException("Pharmacy not found."));

        if (!pharmacy.isApprovedPartner() || !pharmacy.isActive()) {
            throw new IllegalStateException("Pharmacy is not approved and active.");
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

    // Memvalidasi kredensial login dan memastikan akun masih aktif.
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (!user.matchesPassword(request.getPassword(), passwordEncoder)) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        if (!user.isActive()) {
            throw new IllegalStateException("User account is inactive.");
        }

        if (user instanceof Doctor doctor && !doctor.isApprovedPartner()) {
            throw new IllegalStateException("Doctor account is not approved.");
        }

        if (user instanceof Pharmacist pharmacist
                && (!pharmacist.isApprovedPartner()
                || !pharmacist.getPharmacy().isApprovedPartner()
                || !pharmacist.getPharmacy().isActive())) {
            throw new IllegalStateException("Pharmacy account is not approved and active.");
        }

        return toAuthResponse(user);
    }

    private Doctor findDoctorById(Long doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found."));
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
                user.getRole()
        );
    }
}
