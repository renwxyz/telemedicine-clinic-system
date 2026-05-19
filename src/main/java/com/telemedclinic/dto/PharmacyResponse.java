package com.telemedclinic.dto;

import com.telemedclinic.model.Pharmacy;

public class PharmacyResponse {

    private Long pharmacyId;
    private String name;
    private String address;
    private String phoneNumber;
    private double latitude;
    private double longitude;
    private boolean active;

    public PharmacyResponse(Pharmacy pharmacy) {
        this.pharmacyId = pharmacy.getPharmacyId();
        this.name = pharmacy.getName();
        this.address = pharmacy.getAddress();
        this.phoneNumber = pharmacy.getPhoneNumber();
        this.latitude = pharmacy.getLatitude();
        this.longitude = pharmacy.getLongitude();
        this.active = pharmacy.isActive();
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

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean isActive() {
        return active;
    }
}
