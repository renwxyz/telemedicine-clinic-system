package com.telemedclinic.config;

import java.time.LocalDate;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.telemedclinic.user.entity.Admin;
import com.telemedclinic.user.entity.Customer;
import com.telemedclinic.user.entity.Gender;
import com.telemedclinic.pharmacy.internal.repository.PharmacyRepository;
import com.telemedclinic.pharmacy.internal.entity.Pharmacist;
import com.telemedclinic.medicine.entity.Medicine;
import com.telemedclinic.medicine.repository.MedicineRepository;
import com.telemedclinic.pharmacy.internal.entity.InventoryItem;
import com.telemedclinic.pharmacy.internal.repository.InventoryItemRepository;
import com.telemedclinic.user.repository.AdminRepository;
import com.telemedclinic.user.repository.UserRepository;
import com.telemedclinic.user.repository.DoctorRepository;
import com.telemedclinic.pharmacy.internal.entity.PharmacyOwner;
import com.telemedclinic.pharmacy.internal.repository.PharmacyOwnerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private static final String DEFAULT_ADMIN_NAME = "Administrator";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@klinikku.id";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    private static final String DEFAULT_ADMIN_PHONE_NUMBER = "081200000000";
    private static final String DEFAULT_ADMIN_EMPLOYEE_NUMBER = "EMP-0001";

    private static final String DEFAULT_USER_NAME = "Pengguna Default";
    private static final String DEFAULT_USER_EMAIL = "user@klinikku.id";
    private static final String DEFAULT_USER_PASSWORD = "user123";
    private static final String DEFAULT_USER_PHONE_NUMBER = "081211111111";
    private static final String DEFAULT_USER_ADDRESS = "Jl. Sehat No. 1";
    private static final Gender DEFAULT_USER_GENDER = Gender.MALE;
    private static final LocalDate DEFAULT_USER_BIRTH_DATE = LocalDate.of(1990, 1, 1);
    private static final Double DEFAULT_USER_HEIGHT = 170.0;
    private static final Double DEFAULT_USER_WEIGHT = 70.0;

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PharmacyRepository pharmacyRepository;
    private final MedicineRepository medicineRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final PasswordEncoder passwordEncoder;
    private final PharmacyOwnerRepository pharmacyOwnerRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedAdmin();
        seedDefaultUser();
        seedDefaultDoctor();
        seedDefaultPharmacyOwner();
        seedDefaultPharmacy();
        seedDefaultPharmacist();
        seedMedicines();
    }

    private void seedDefaultDoctor() {
        final String DEFAULT_DOCTOR_NAME = "Dr. Ahmad Fauzi";
        final String DEFAULT_DOCTOR_EMAIL = "doctor@klinikku.id";
        final String DEFAULT_DOCTOR_PASSWORD = "doctor123";
        final String DEFAULT_DOCTOR_PHONE = "081299988877";
        final String DEFAULT_DOCTOR_LICENSE = "SIP-2025-001";
        final String DEFAULT_DOCTOR_SPECIALIZATION = "Umum";

        if (userRepository.existsByEmail(DEFAULT_DOCTOR_EMAIL)) {
            log.info("Doctor default sudah ada, skip seeding doctor");
            userRepository.findByEmail(DEFAULT_DOCTOR_EMAIL).ifPresent(user -> {
                if (user instanceof com.telemedclinic.user.entity.Doctor doc) {
                    if (doc.getBalance() == null || doc.getBalance() == 0.0) {
                        doc.setBalance(2500000.0);
                        doc.setConsultationFee(250000.0);
                        doctorRepository.save(doc);
                        log.info("Default doctor wallet & fee initialized.");
                    }
                }
            });
            return;
        }

        com.telemedclinic.user.entity.Doctor doctor = new com.telemedclinic.user.entity.Doctor(
                DEFAULT_DOCTOR_NAME,
                DEFAULT_DOCTOR_EMAIL,
                passwordEncoder.encode(DEFAULT_DOCTOR_PASSWORD),
                DEFAULT_DOCTOR_PHONE,
                DEFAULT_DOCTOR_SPECIALIZATION,
                DEFAULT_DOCTOR_LICENSE
        );

        // set as approved so the account can login immediately
        doctor.approveApplication();
        doctor.setBalance(2500000.0);
        doctor.setConsultationFee(250000.0);

        doctorRepository.save(doctor);
        log.info("Doctor default berhasil dibuat: {}", DEFAULT_DOCTOR_EMAIL);
    }

    private void seedDefaultPharmacyOwner() {
        final String DEFAULT_OWNER_NAME = "Budi Santoso";
        final String DEFAULT_OWNER_EMAIL = "owner@klinikku.id";
        final String DEFAULT_OWNER_PASSWORD = "owner123";
        final String DEFAULT_OWNER_PHONE = "081255556666";
        final String DEFAULT_OWNER_NIK = "3170000000000001";

        if (userRepository.existsByEmail(DEFAULT_OWNER_EMAIL)) {
            log.info("Pharmacy Owner default sudah ada, skip seeding owner");
            return;
        }

        PharmacyOwner owner = new PharmacyOwner(
                DEFAULT_OWNER_NAME,
                DEFAULT_OWNER_EMAIL,
                passwordEncoder.encode(DEFAULT_OWNER_PASSWORD),
                DEFAULT_OWNER_PHONE,
                DEFAULT_OWNER_NIK
        );

        pharmacyOwnerRepository.save(owner);
        log.info("Pharmacy Owner default berhasil dibuat: {}", DEFAULT_OWNER_EMAIL);
    }

    private void seedDefaultPharmacy() {
        final String DEFAULT_PHARMACY_NAME = "Apotek Medika Farma";
        final String DEFAULT_PHARMACY_ADDRESS = "Jl. Raya Kebayoran No. 45, Jakarta Selatan";
        final String DEFAULT_PHARMACY_PHONE = "+62-21-1234567";
        final String DEFAULT_PHARMACY_LEGAL_DOC = "SIA-2024-001234";
        final double DEFAULT_PHARMACY_LATITUDE = -6.244667;
        final double DEFAULT_PHARMACY_LONGITUDE = 106.800906;

        if (pharmacyRepository.existsByLegalDocumentNumber(DEFAULT_PHARMACY_LEGAL_DOC)) {
            log.info("Pharmacy default sudah ada, skip seeding pharmacy");
            return;
        }

        com.telemedclinic.pharmacy.internal.entity.Pharmacy pharmacy = new com.telemedclinic.pharmacy.internal.entity.Pharmacy(
                DEFAULT_PHARMACY_NAME,
                DEFAULT_PHARMACY_ADDRESS,
                DEFAULT_PHARMACY_PHONE,
                DEFAULT_PHARMACY_LEGAL_DOC,
                DEFAULT_PHARMACY_LATITUDE,
                DEFAULT_PHARMACY_LONGITUDE
        );

        userRepository.findByEmail("owner@klinikku.id").ifPresent(user -> {
            if (user instanceof PharmacyOwner owner) {
                pharmacy.setOwner(owner);
            }
        });

        pharmacyRepository.save(pharmacy);
        log.info("Pharmacy default berhasil dibuat: {}", DEFAULT_PHARMACY_NAME);
    }

    private void seedDefaultPharmacist() {
        final String DEFAULT_PHARMACIST_NAME = "Dian Salsabila";
        final String DEFAULT_PHARMACIST_EMAIL = "pharmacist@klinikku.id";
        final String DEFAULT_PHARMACIST_PASSWORD = "pharmacist123";
        final String DEFAULT_PHARMACIST_PHONE = "081244443333";
        final String DEFAULT_PHARMACIST_LICENSE = "SIK-2025-00999";
        final String DEFAULT_PHARMACY_LEGAL_DOC = "SIA-2024-001234";

        if (userRepository.existsByEmail(DEFAULT_PHARMACIST_EMAIL)) {
            log.info("Pharmacist default sudah ada, skip seeding pharmacist");
            return;
        }

        com.telemedclinic.pharmacy.internal.entity.Pharmacy pharmacy = pharmacyRepository.findByLegalDocumentNumber(DEFAULT_PHARMACY_LEGAL_DOC)
                .orElseThrow(() -> new IllegalStateException("Default pharmacy not found for pharmacist seeding."));

        Pharmacist pharmacist = new Pharmacist(
                DEFAULT_PHARMACIST_NAME,
                DEFAULT_PHARMACIST_EMAIL,
                passwordEncoder.encode(DEFAULT_PHARMACIST_PASSWORD),
                DEFAULT_PHARMACIST_PHONE,
                DEFAULT_PHARMACIST_LICENSE,
                pharmacy
        );

        pharmacist.clearMustChangePassword();

        userRepository.save(pharmacist);
        log.info("Pharmacist default berhasil dibuat: {}", DEFAULT_PHARMACIST_EMAIL);
    }

    private void seedAdmin() {
        if (adminRepository.existsByEmail(DEFAULT_ADMIN_EMAIL)) {
            log.info("Admin default sudah ada, skip seeding admin");
            return;
        }

        Admin admin = new Admin(
                DEFAULT_ADMIN_NAME,
                DEFAULT_ADMIN_EMAIL,
                passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD),
                DEFAULT_ADMIN_PHONE_NUMBER,
                DEFAULT_ADMIN_EMPLOYEE_NUMBER
        );

        adminRepository.save(admin);
        log.info("Admin default berhasil dibuat");
    }

    private void seedDefaultUser() {
        if (userRepository.existsByEmail(DEFAULT_USER_EMAIL)) {
            log.info("User default sudah ada, skip seeding user");
            return;
        }

        Customer user = new Customer(
                DEFAULT_USER_NAME,
                DEFAULT_USER_EMAIL,
                passwordEncoder.encode(DEFAULT_USER_PASSWORD),
                DEFAULT_USER_PHONE_NUMBER,
                DEFAULT_USER_ADDRESS,
                DEFAULT_USER_GENDER,
                DEFAULT_USER_BIRTH_DATE,
                DEFAULT_USER_HEIGHT,
                DEFAULT_USER_WEIGHT
        );

        userRepository.save(user);
        log.info("User default berhasil dibuat");
    }

    private void seedMedicines() {
        if (medicineRepository.count() > 0) {
            log.info("Medicines sudah ada, skip seeding medicines");
            return;
        }

        com.telemedclinic.pharmacy.internal.entity.Pharmacy pharmacy = pharmacyRepository.findByLegalDocumentNumber("SIA-2024-001234")
                .orElseThrow(() -> new IllegalStateException("Default pharmacy not found for medicine seeding."));

        Medicine paracetamol = new Medicine(
                "Paracetamol 500mg",
                "Obat penurun panas dan pereda nyeri ringan hingga sedang.",
                "Analgesik",
                false,
                "https://placehold.co/400x400/FFECD1/FF9800?text=Paracetamol"
        );

        Medicine vitaminC = new Medicine(
                "Vitamin C 1000mg",
                "Suplemen vitamin C untuk menjaga daya tahan tubuh.",
                "Vitamin & Suplemen",
                false,
                "https://placehold.co/400x400/E8F5E9/4CAF50?text=Vitamin+C"
        );

        Medicine amoxicillin = new Medicine(
                "Amoxicillin 500mg",
                "Antibiotik penisilin yang digunakan untuk mengobati berbagai macam infeksi bakteri.",
                "Antibiotik",
                true,
                "https://placehold.co/400x400/FFEBEE/F44336?text=Amoxicillin"
        );

        medicineRepository.save(paracetamol);
        medicineRepository.save(vitaminC);
        medicineRepository.save(amoxicillin);

        InventoryItem item1 = new InventoryItem(paracetamol, pharmacy, 100, 15000.0);
        InventoryItem item2 = new InventoryItem(vitaminC, pharmacy, 50, 25000.0);
        InventoryItem item3 = new InventoryItem(amoxicillin, pharmacy, 20, 35000.0);

        inventoryItemRepository.save(item1);
        inventoryItemRepository.save(item2);
        inventoryItemRepository.save(item3);

        log.info("Dummy medicines and inventory items berhasil dibuat");
    }
}
