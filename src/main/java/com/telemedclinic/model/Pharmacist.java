package com.telemedclinic.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
public class Pharmacist extends User {

    // Attributes
    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false, unique = true)
    private String licenseNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PartnerApplicationStatus applicationStatus;


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
                Role.ROLE_PHARMACIST
        );

        setPhoneNumber(phoneNumber);
        setLicenseNumber(licenseNumber);
        setPharmacy(pharmacy);
        this.applicationStatus = PartnerApplicationStatus.APPROVED;
    }


    // Getter
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public Pharmacy getPharmacy() {
        return pharmacy;
    }

    public PartnerApplicationStatus getApplicationStatus() {
        return applicationStatus;
    }


    // Setter
    public void setPhoneNumber(String phoneNumber) {

        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Phone number cannot be empty."
            );
        }

        this.phoneNumber = phoneNumber;
    }

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

    public void setApplicationStatus(PartnerApplicationStatus applicationStatus) {

        if (applicationStatus == null) {
            throw new IllegalArgumentException(
                    "Application status cannot be null."
            );
        }

        this.applicationStatus = applicationStatus;
    }


    // Behavior methods
    public void updateProfile(
            String name,
            String phoneNumber
    ) {

        super.updateProfile(name);
        setPhoneNumber(phoneNumber);
    }

    public boolean isAssignedTo(Pharmacy pharmacy) {

        if (pharmacy == null) {
            return false;
        }

        if (this.pharmacy.getPharmacyId() == null || pharmacy.getPharmacyId() == null) {
            return this.pharmacy == pharmacy;
        }

        return this.pharmacy.getPharmacyId().equals(pharmacy.getPharmacyId());
    }

    public boolean isApprovedPartner() {
        return PartnerApplicationStatus.APPROVED.equals(applicationStatus);
    }
}
