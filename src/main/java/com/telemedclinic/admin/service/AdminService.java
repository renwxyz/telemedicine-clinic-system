package com.telemedclinic.admin.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.telemedclinic.admin.dto.AdminDashboardStats;
import com.telemedclinic.auth.dto.AuthResponse;
import com.telemedclinic.auth.service.AuthService;
import com.telemedclinic.auth.service.DoctorProvisioningResult;
import com.telemedclinic.auth.service.DoctorProvisioningService;
import com.telemedclinic.auth.service.OwnerProvisioningResult;
import com.telemedclinic.auth.service.OwnerProvisioningService;
import com.telemedclinic.user.dto.CreateOwnerRequest;
import com.telemedclinic.user.dto.CreateDoctorForm;
import com.telemedclinic.user.dto.CreateDoctorRequest;
import com.telemedclinic.user.dto.CreatePharmacistForm;
import com.telemedclinic.user.dto.CreatePharmacistRequest;
import com.telemedclinic.pharmacy.internal.dto.CreatePharmacyForm;
import com.telemedclinic.pharmacy.internal.dto.PharmacyRegisterRequest;
import com.telemedclinic.pharmacy.internal.entity.Pharmacy;
import com.telemedclinic.pharmacy.internal.service.PharmacyService;
import com.telemedclinic.user.entity.Role;
import com.telemedclinic.user.entity.User;
import com.telemedclinic.pharmacy.internal.repository.PharmacyRepository;
import com.telemedclinic.user.repository.UserRepository;

@Service
public class AdminService {

    private final AuthService authService;
    private final DoctorProvisioningService doctorProvisioningService;
    private final OwnerProvisioningService ownerProvisioningService;
    private final PharmacyService pharmacyService;
    private final PharmacyRepository pharmacyRepository;
    private final UserRepository userRepository;
    private final com.telemedclinic.medicine.repository.MedicineRepository medicineRepository;

    public AdminService(
            AuthService authService,
            DoctorProvisioningService doctorProvisioningService,
            OwnerProvisioningService ownerProvisioningService,
            PharmacyService pharmacyService,
            PharmacyRepository pharmacyRepository,
            UserRepository userRepository,
            com.telemedclinic.medicine.repository.MedicineRepository medicineRepository
    ) {

        this.authService = authService;
        this.doctorProvisioningService = doctorProvisioningService;
        this.ownerProvisioningService = ownerProvisioningService;
        this.pharmacyService = pharmacyService;
        this.pharmacyRepository = pharmacyRepository;
        this.userRepository = userRepository;
        this.medicineRepository = medicineRepository;
    }

    // Membuat pharmacy baru dari input admin beserta dengan akun ownernya.
    @Transactional
    public OwnerProvisioningResult createPharmacy(CreatePharmacyForm form) {
        // 1. Buat Pharmacy Owner
        OwnerProvisioningResult ownerResult = ownerProvisioningService.provisionOwner(
                new CreateOwnerRequest(
                        form.getOwnerName(),
                        form.getOwnerEmail(),
                        form.getOwnerPhoneNumber()
                )
        );

        // 2. Buat Pharmacy
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

        // 3. Kaitkan
        ownerResult.getOwner().addPharmacy(pharmacy);
        
        pharmacy.approveApplication();
        pharmacyRepository.save(pharmacy);

        return ownerResult;
    }

    // Mengambil semua pharmacy yang terdaftar untuk kebutuhan halaman admin.
    public List<Pharmacy> findAllPharmacies() {
        return pharmacyRepository.findAll();
    }

    public List<Pharmacy> findPharmacies(String search) {
        List<Pharmacy> pharmacies = findAllPharmacies();

        if (search == null || search.isBlank()) {
            return pharmacies;
        }

        String keyword = search.toLowerCase(Locale.ROOT);

        return pharmacies.stream()
                .filter(pharmacy -> containsIgnoreCase(pharmacy.getName(), keyword)
                        || containsIgnoreCase(pharmacy.getAddress(), keyword)
                        || containsIgnoreCase(pharmacy.getPhoneNumber(), keyword)
                        || containsIgnoreCase(pharmacy.getLegalDocumentNumber(), keyword))
                .toList();
    }

    // Membuat akun doctor baru dari input admin dengan status langsung approved.
    public DoctorProvisioningResult createDoctor(CreateDoctorForm form) {
        return doctorProvisioningService.provisionDoctor(
                new CreateDoctorRequest(
                        form.getName(),
                        form.getEmail(),
                        form.getPhoneNumber(),
                        form.getSpecialization(),
                        form.getLicenseNumber()
                )
        );
    }

    public DoctorProvisioningResult resendDoctorCredentials(Long doctorId) {
        return doctorProvisioningService.resendCredentials(doctorId);
    }


    // Mengambil semua user dari seluruh role untuk kebutuhan manajemen akun admin.
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    // Mengambil akun doctor dan pharmacist terbaru untuk ringkasan dashboard admin.
    public List<User> findRecentMedicalStaffUsers() {
        return userRepository.findTop5ByRoleInOrderByCreatedAtDesc(List.of(
                Role.ROLE_DOCTOR,
                Role.ROLE_PHARMACIST
        ));
    }

    // Mengambil pharmacy terbaru untuk ringkasan dashboard admin.
    public List<Pharmacy> findRecentPharmacies() {
        return pharmacyRepository.findTop5ByOrderByPharmacyIdDesc();
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

    // Mengambil semua master data obat
    public List<com.telemedclinic.medicine.entity.Medicine> findAllMedicines() {
        return medicineRepository.findAll();
    }

    public List<com.telemedclinic.medicine.entity.Medicine> findMedicines(String search) {
        List<com.telemedclinic.medicine.entity.Medicine> medicines = findAllMedicines();

        if (search == null || search.isBlank()) {
            return medicines;
        }

        String keyword = search.toLowerCase(Locale.ROOT);

        return medicines.stream()
                .filter(med -> containsIgnoreCase(med.getName(), keyword)
                        || containsIgnoreCase(med.getCategory(), keyword))
                .toList();
    }

    // Menambah master data obat baru
    @Transactional
    public com.telemedclinic.medicine.entity.Medicine createMedicine(com.telemedclinic.admin.dto.CreateMedicineForm form) {
        com.telemedclinic.medicine.entity.Medicine medicine = new com.telemedclinic.medicine.entity.Medicine(
                form.getName(),
                form.getDescription(),
                form.getCategory(),
                form.isRequiresPrescription(),
                form.getImageUrl()
        );
        return medicineRepository.save(medicine);
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}
