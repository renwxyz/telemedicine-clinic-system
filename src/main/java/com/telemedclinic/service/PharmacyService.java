package com.telemedclinic.service;

import org.springframework.stereotype.Service;

import com.telemedclinic.dto.PharmacyRegisterRequest;
import com.telemedclinic.model.Pharmacy;
import com.telemedclinic.repository.PharmacyRepository;

@Service
public class PharmacyService {

    private final PharmacyRepository pharmacyRepository;

    public PharmacyService(PharmacyRepository pharmacyRepository) {
        this.pharmacyRepository = pharmacyRepository;
    }

    public Pharmacy registerPharmacy(PharmacyRegisterRequest request) {
        Pharmacy pharmacy = new Pharmacy(
                request.getName(),
                request.getAddress(),
                request.getPhoneNumber(),
                request.getLatitude(),
                request.getLongitude()
        );

        return pharmacyRepository.save(pharmacy);
    }

    public void activatePharmacy(Long pharmacyId) {
        Pharmacy pharmacy = findPharmacyById(pharmacyId);
        pharmacy.activate();
        pharmacyRepository.save(pharmacy);
    }

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
