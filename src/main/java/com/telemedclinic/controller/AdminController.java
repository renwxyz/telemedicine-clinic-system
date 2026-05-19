package com.telemedclinic.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.telemedclinic.dto.AuthResponse;
import com.telemedclinic.dto.CreatePharmacistAccountRequest;
import com.telemedclinic.dto.PharmacyRegisterRequest;
import com.telemedclinic.dto.PharmacyResponse;
import com.telemedclinic.model.Pharmacy;
import com.telemedclinic.service.AuthService;
import com.telemedclinic.service.PharmacyService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AuthService authService;
    private final PharmacyService pharmacyService;

    public AdminController(
            AuthService authService,
            PharmacyService pharmacyService
    ) {

        this.authService = authService;
        this.pharmacyService = pharmacyService;
    }

    @PostMapping("/pharmacies")
    public ResponseEntity<PharmacyResponse> registerPharmacy(
            @RequestBody PharmacyRegisterRequest request
    ) {

        Pharmacy pharmacy = pharmacyService.registerPharmacy(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new PharmacyResponse(pharmacy));
    }

    @PostMapping("/pharmacists")
    public ResponseEntity<AuthResponse> createPharmacistAccount(
            @RequestBody CreatePharmacistAccountRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.createPharmacistAccount(request));
    }

    @PatchMapping("/pharmacies/{pharmacyId}/activate")
    public ResponseEntity<Void> activatePharmacy(
            @PathVariable Long pharmacyId
    ) {

        pharmacyService.activatePharmacy(pharmacyId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/pharmacies/{pharmacyId}/deactivate")
    public ResponseEntity<Void> deactivatePharmacy(
            @PathVariable Long pharmacyId
    ) {

        pharmacyService.deactivatePharmacy(pharmacyId);
        return ResponseEntity.noContent().build();
    }
}
