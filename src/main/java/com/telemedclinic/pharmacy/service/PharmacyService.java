package com.telemedclinic.pharmacy.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.telemedclinic.pharmacy.dto.PharmacyRegisterRequest;
import com.telemedclinic.pharmacy.entity.Pharmacy;
import com.telemedclinic.pharmacy.repository.PharmacyRepository;

@Service
public class PharmacyService {

    private final PharmacyRepository pharmacyRepository;

    public PharmacyService(PharmacyRepository pharmacyRepository) {
        this.pharmacyRepository = pharmacyRepository;
    }

    // Mendaftarkan pharmacy baru menggunakan data input yang diterima service.
    @Transactional
    public Pharmacy registerPharmacy(PharmacyRegisterRequest request) {
        Pharmacy pharmacy = new Pharmacy(
                request.getName(),
                request.getAddress(),
                request.getPhoneNumber(),
                request.getLegalDocumentNumber(),
                request.getLatitude(),
                request.getLongitude()
        );

        return pharmacyRepository.save(pharmacy);
    }

    // Menyetujui aplikasi pharmacy dan mengaktifkannya.
    @Transactional
    public Pharmacy approvePharmacy(Long pharmacyId) {
        Pharmacy pharmacy = findPharmacyById(pharmacyId);
        pharmacy.approveApplication();
        return pharmacyRepository.save(pharmacy);
    }

    // Menolak aplikasi pharmacy dan menonaktifkannya.
    @Transactional
    public Pharmacy declinePharmacy(Long pharmacyId) {
        Pharmacy pharmacy = findPharmacyById(pharmacyId);
        pharmacy.declineApplication();
        return pharmacyRepository.save(pharmacy);
    }

    // Mengaktifkan pharmacy yang sudah approved.
    @Transactional
    public void activatePharmacy(Long pharmacyId) {
        Pharmacy pharmacy = findPharmacyById(pharmacyId);
        if (!pharmacy.isApprovedPartner()) {
            throw new IllegalStateException("Pharmacy application is not approved.");
        }

        pharmacy.activate();
        pharmacyRepository.save(pharmacy);
    }

    // Menonaktifkan pharmacy.
    @Transactional
    public void deactivatePharmacy(Long pharmacyId) {
        Pharmacy pharmacy = findPharmacyById(pharmacyId);
        pharmacy.deactivate();
        pharmacyRepository.save(pharmacy);
    }

    private Pharmacy findPharmacyById(Long pharmacyId) {
        return pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new IllegalArgumentException("Pharmacy not found."));
    }
}
