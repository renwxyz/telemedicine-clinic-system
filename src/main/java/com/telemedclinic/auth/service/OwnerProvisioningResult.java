package com.telemedclinic.auth.service;

import com.telemedclinic.pharmacy.internal.entity.PharmacyOwner;

public class OwnerProvisioningResult {

    private final PharmacyOwner owner;
    private final boolean emailSent;
    private final String errorMessage;

    private OwnerProvisioningResult(
            PharmacyOwner owner,
            boolean emailSent,
            String errorMessage
    ) {
        this.owner = owner;
        this.emailSent = emailSent;
        this.errorMessage = errorMessage;
    }

    public static OwnerProvisioningResult success(PharmacyOwner owner) {
        return new OwnerProvisioningResult(owner, true, null);
    }

    public static OwnerProvisioningResult emailFailed(
            PharmacyOwner owner,
            String errorMessage
    ) {
        return new OwnerProvisioningResult(owner, false, errorMessage);
    }

    public PharmacyOwner getOwner() {
        return owner;
    }

    public boolean isEmailSent() {
        return emailSent;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
