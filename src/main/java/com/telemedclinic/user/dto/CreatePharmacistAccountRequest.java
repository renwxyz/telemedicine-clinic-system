package com.telemedclinic.user.dto;

public class CreatePharmacistAccountRequest {

    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private String licenseNumber;
    private Long pharmacyId;

    public CreatePharmacistAccountRequest() {
    }

    public CreatePharmacistAccountRequest(
            String name,
            String email,
            String password,
            String phoneNumber,
            String licenseNumber,
            Long pharmacyId
    ) {

        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.licenseNumber = licenseNumber;
        this.pharmacyId = pharmacyId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public Long getPharmacyId() {
        return pharmacyId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public void setPharmacyId(Long pharmacyId) {
        this.pharmacyId = pharmacyId;
    }
}
