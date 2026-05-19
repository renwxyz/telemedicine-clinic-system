package com.telemedclinic.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;

@Entity
public class Doctor extends User {

    // Attributes
    @Column(nullable = false)
    private String specialization;

    @Column(nullable = false, unique = true)
    private String licenseNumber;


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
                phoneNumber
        );

        setSpecialization(specialization);
        setLicenseNumber(licenseNumber);
    }


    // Getter
    public String getSpecialization() {
        return specialization;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }


    // Setter
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


    // Behavior methods
    public boolean isSpecialist(String specialization) {

        if (specialization == null) {
            return false;
        }

        return this.specialization.equalsIgnoreCase(
                specialization
        );
    }
}
