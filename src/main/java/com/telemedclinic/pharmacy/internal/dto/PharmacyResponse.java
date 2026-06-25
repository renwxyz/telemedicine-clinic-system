package com.telemedclinic.pharmacy.internal.dto;

import com.telemedclinic.pharmacy.internal.entity.Pharmacy;
import com.telemedclinic.user.entity.PartnerApplicationStatus;

public class PharmacyResponse {

    private Long pharmacyId;
    private String name;
    private String address;
    private String phoneNumber;
    private String legalDocumentNumber;
    private double latitude;
    private double longitude;
    private boolean active;
    private PartnerApplicationStatus applicationStatus;

    public PharmacyResponse(Pharmacy pharmacy) {
        this.pharmacyId = pharmacy.getPharmacyId();
        this.name = pharmacy.getName();
        this.address = pharmacy.getAddress();
        this.phoneNumber = pharmacy.getPhoneNumber();
        this.legalDocumentNumber = pharmacy.getLegalDocumentNumber();
        this.latitude = pharmacy.getLatitude();
        this.longitude = pharmacy.getLongitude();
        this.active = pharmacy.isActive();
        this.applicationStatus = pharmacy.getApplicationStatus();
    }

    public Long getPharmacyId() {
        return pharmacyId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getLegalDocumentNumber() {
        return legalDocumentNumber;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean isActive() {
        return active;
    }

    public PartnerApplicationStatus getApplicationStatus() {
        return applicationStatus;
    }
}
