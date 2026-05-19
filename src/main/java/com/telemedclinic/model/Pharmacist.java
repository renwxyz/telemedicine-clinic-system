package com.telemedclinic.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;

@Entity
public class Pharmacist extends User {

    // Attributes
    @Column(nullable = false, unique = true)
    private String licenseNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;


    // No-args constructor for JPA
    public Pharmacist() {
    }


    // Constructor
    public Pharmacist(
            String name,
            String email,
            String password,
            String phoneNumber,
            String licenseNumber,
            Pharmacy pharmacy
    ) {

        super(
                name,
                email,
                password,
                phoneNumber
        );

        setLicenseNumber(licenseNumber);
        setPharmacy(pharmacy);
    }


    // Getter
    public String getLicenseNumber() {
        return licenseNumber;
    }

    public Pharmacy getPharmacy() {
        return pharmacy;
    }


    // Setter
    public void setLicenseNumber(String licenseNumber) {

        if (licenseNumber == null || licenseNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "License number cannot be empty."
            );
        }

        this.licenseNumber = licenseNumber;
    }

    public void setPharmacy(Pharmacy pharmacy) {

        if (pharmacy == null) {
            throw new IllegalArgumentException(
                    "Pharmacy cannot be null."
            );
        }

        this.pharmacy = pharmacy;
    }


    // Behavior methods
    public boolean isAssignedTo(Pharmacy pharmacy) {

        if (pharmacy == null) {
            return false;
        }

        if (this.pharmacy.getPharmacyId() == null || pharmacy.getPharmacyId() == null) {
            return this.pharmacy == pharmacy;
        }

        return this.pharmacy.getPharmacyId().equals(pharmacy.getPharmacyId());
    }
}
