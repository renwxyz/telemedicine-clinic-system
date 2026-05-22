package com.telemedclinic.user.dto;

public class DoctorRegisterRequest {

    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private String specialization;
    private String licenseNumber;

    public DoctorRegisterRequest() {
    }

    public DoctorRegisterRequest(
            String name,
            String email,
            String password,
            String phoneNumber,
            String specialization,
            String licenseNumber
    ) {

        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.specialization = specialization;
        this.licenseNumber = licenseNumber;
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

    public String getSpecialization() {
        return specialization;
    }

    public String getLicenseNumber() {
        return licenseNumber;
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

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }
}
