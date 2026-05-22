package com.telemedclinic.user.dto;

import com.telemedclinic.user.entity.Doctor;
import com.telemedclinic.user.entity.PartnerApplicationStatus;

public class DoctorResponse {

    private Long doctorId;
    private String name;
    private String email;
    private String phoneNumber;
    private String specialization;
    private String licenseNumber;
    private PartnerApplicationStatus applicationStatus;

    public DoctorResponse(Doctor doctor) {
        this.doctorId = doctor.getUserId();
        this.name = doctor.getName();
        this.email = doctor.getEmail();
        this.phoneNumber = doctor.getPhoneNumber();
        this.specialization = doctor.getSpecialization();
        this.licenseNumber = doctor.getLicenseNumber();
        this.applicationStatus = doctor.getApplicationStatus();
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
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

    public PartnerApplicationStatus getApplicationStatus() {
        return applicationStatus;
    }
}
