package com.telemedclinic.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.telemedclinic.dto.AdminDashboardStats;
import com.telemedclinic.dto.AuthResponse;
import com.telemedclinic.dto.CreateDoctorForm;
import com.telemedclinic.dto.CreatePharmacistAccountRequest;
import com.telemedclinic.dto.CreatePharmacistForm;
import com.telemedclinic.dto.CreatePharmacyForm;
import com.telemedclinic.dto.DoctorRegisterRequest;
import com.telemedclinic.dto.PharmacyRegisterRequest;
import com.telemedclinic.model.Doctor;
import com.telemedclinic.model.Pharmacy;
import com.telemedclinic.model.Role;
import com.telemedclinic.model.User;
import com.telemedclinic.repository.PharmacyRepository;
import com.telemedclinic.repository.UserRepository;

@Service
public class AdminService {

    private final AuthService authService;
    private final PharmacyService pharmacyService;
    private final PharmacyRepository pharmacyRepository;
    private final UserRepository userRepository;

    public AdminService(
            AuthService authService,
            PharmacyService pharmacyService,
            PharmacyRepository pharmacyRepository,
            UserRepository userRepository
    ) {

        this.authService = authService;
        this.pharmacyService = pharmacyService;
        this.pharmacyRepository = pharmacyRepository;
        this.userRepository = userRepository;
    }

    // Membuat pharmacy baru dari input admin dengan status langsung approved dan aktif.
    @Transactional
    public Pharmacy createPharmacy(CreatePharmacyForm form) {
        Pharmacy pharmacy = pharmacyService.registerPharmacy(
                new PharmacyRegisterRequest(
                        form.getName(),
                        form.getAddress(),
                        form.getPhoneNumber(),
                        form.getLegalDocumentNumber(),
                        form.getLatitude(),
                        form.getLongitude()
                )
        );

        pharmacy.approveApplication();
        return pharmacyRepository.save(pharmacy);
    }

    // Mengambil semua pharmacy yang terdaftar untuk kebutuhan halaman admin.
    public List<Pharmacy> findAllPharmacies() {
        return pharmacyRepository.findAll();
    }

    // Membuat akun doctor baru dari input admin dengan status langsung approved.
    @Transactional
    public Doctor createDoctor(CreateDoctorForm form) {
        Doctor doctor = authService.registerDoctor(
                new DoctorRegisterRequest(
                        form.getName(),
                        form.getEmail(),
                        form.getPassword(),
                        form.getPhoneNumber(),
                        form.getSpecialization(),
                        form.getLicenseNumber()
                )
        );

        doctor.approveApplication();
        return authService.saveDoctor(doctor);
    }

    // Membuat akun pharmacist baru untuk pharmacy yang sudah dipilih admin.
    @Transactional
    public AuthResponse createPharmacist(CreatePharmacistForm form) {
        return authService.createPharmacistAccount(
                new CreatePharmacistAccountRequest(
                        form.getName(),
                        form.getEmail(),
                        form.getPassword(),
                        form.getPhoneNumber(),
                        form.getLicenseNumber(),
                        form.getPharmacyId()
                )
        );
    }

    // Mengambil semua user dari seluruh role untuk kebutuhan manajemen akun admin.
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    // Mengaktifkan atau menonaktifkan user berdasarkan userId.
    @Transactional
    public User toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        user.toggleActiveStatus();
        return userRepository.save(user);
    }

    // Menghitung agregasi data utama untuk dashboard admin.
    public AdminDashboardStats getDashboardStats() {
        return AdminDashboardStats.builder()
                .totalDoctor(userRepository.countByRole(Role.ROLE_DOCTOR))
                .totalPharmacist(userRepository.countByRole(Role.ROLE_PHARMACIST))
                .totalCustomer(userRepository.countByRole(Role.ROLE_CUSTOMER))
                .totalPharmacy(pharmacyRepository.count())
                .totalInactiveUser(userRepository.countByActiveFalse())
                .build();
    }
}
