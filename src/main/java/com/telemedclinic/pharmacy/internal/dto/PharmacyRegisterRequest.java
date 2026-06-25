package com.telemedclinic.pharmacy.internal.dto;

public class PharmacyRegisterRequest {

    private String name;
    private String address;
    private String phoneNumber;
    private String legalDocumentNumber;
    private double latitude;
    private double longitude;

    public PharmacyRegisterRequest() {
    }

    public PharmacyRegisterRequest(
            String name,
            String address,
            String phoneNumber,
            String legalDocumentNumber,
            double latitude,
            double longitude
    ) {

        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.legalDocumentNumber = legalDocumentNumber;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setLegalDocumentNumber(String legalDocumentNumber) {
        this.legalDocumentNumber = legalDocumentNumber;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
