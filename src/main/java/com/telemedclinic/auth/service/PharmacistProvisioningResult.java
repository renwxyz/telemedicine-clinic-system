package com.telemedclinic.auth.service;

import com.telemedclinic.pharmacy.internal.entity.Pharmacist;

public class PharmacistProvisioningResult {

    private final Pharmacist pharmacist;
    private final boolean emailSent;
    private final String emailError;

    private PharmacistProvisioningResult(
            Pharmacist pharmacist,
            boolean emailSent,
            String emailError
    ) {

        this.pharmacist = pharmacist;
        this.emailSent = emailSent;
        this.emailError = emailError;
    }

    public static PharmacistProvisioningResult success(Pharmacist pharmacist) {
        return new PharmacistProvisioningResult(pharmacist, true, null);
    }

    public static PharmacistProvisioningResult emailFailed(
            Pharmacist pharmacist,
            String errorMessage
    ) {

        return new PharmacistProvisioningResult(pharmacist, false, errorMessage);
    }

    public Pharmacist getPharmacist() {
        return pharmacist;
    }

    public boolean isEmailSent() {
        return emailSent;
    }

    public String getEmailError() {
        return emailError;
    }
}
