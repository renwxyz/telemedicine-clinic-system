package com.telemedclinic.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
public class Doctor extends User {

    // Attributes
    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String specialization;

    @Column(nullable = false, unique = true)
    private String licenseNumber;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PartnerApplicationStatus applicationStatus;


    // No-args constructor for JPA
    public Doctor() {
    }


    // Constructor
    public Doctor(
            String name,
            String email,
            String password,
            String phoneNumber,
            String specialization,
            String licenseNumber
    ) {

        super(
                name,
                email,
                password,
                Role.ROLE_DOCTOR
        );

        setPhoneNumber(phoneNumber);
        setSpecialization(specialization);
        setLicenseNumber(licenseNumber);
        this.applicationStatus = PartnerApplicationStatus.PENDING;
    }


    // Getter
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getLicenseNumber() {
        return licenseNumber;
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

    public void setSpecialization(String specialization) {

        if (specialization == null || specialization.isBlank()) {
            throw new IllegalArgumentException(
                    "Specialization cannot be empty."
            );
        }

        this.specialization = specialization;
    }

    public void setLicenseNumber(String licenseNumber) {

        if (licenseNumber == null || licenseNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "License number cannot be empty."
            );
        }

        this.licenseNumber = licenseNumber;
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

    public boolean isSpecialist(String specialization) {

        if (specialization == null) {
            return false;
        }

        return this.specialization.equalsIgnoreCase(
                specialization
        );
    }

    public void approveApplication() {
        setApplicationStatus(PartnerApplicationStatus.APPROVED);
    }

    public void declineApplication() {
        setApplicationStatus(PartnerApplicationStatus.DECLINED);
    }

    public boolean isApprovedPartner() {
        return PartnerApplicationStatus.APPROVED.equals(applicationStatus);
    }
}
