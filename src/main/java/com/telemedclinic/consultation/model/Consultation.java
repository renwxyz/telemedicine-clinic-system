package com.telemedclinic.consultation.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import com.telemedclinic.user.entity.Customer;
import com.telemedclinic.user.entity.Doctor;

@Entity
@Table(name = "consultations")
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Column(nullable = false)
    private String complaint;

    private String additionalInfo;

    @Enumerated(EnumType.STRING)
    private ConsultationStatus status;

    private LocalDateTime createdAt;

    private boolean prescription;

    @Column(length = 255)
    private String snapToken;

    @Column(name = "midtrans_order_id", length = 255)
    private String midtransOrderId;

    public Consultation() {}

    public Consultation(Customer customer, Doctor doctor, String complaint, String additionalInfo) {
        this.customer = customer;
        this.doctor = doctor;
        this.complaint = complaint;
        this.additionalInfo = additionalInfo;
        this.status = ConsultationStatus.PENDING;
        this.prescription = false;
    }

    @PrePersist
    protected void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ConsultationStatus.PENDING;
        }
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public String getComplaint() {
        return complaint;
    }

    public void setComplaint(String complaint) {
        this.complaint = complaint;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public ConsultationStatus getStatus() {
        return status;
    }

    public void setStatus(ConsultationStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isPrescription() {
        return prescription;
    }

    public void setPrescription(boolean prescription) {
        this.prescription = prescription;
    }

    public Long getDoctorId() {
        return doctor != null ? doctor.getUserId() : null;
    }

    public String getDoctorName() {
        return doctor != null ? doctor.getName() : "Dokter Umum";
    }

    public String getDoctorSpecialization() {
        return doctor != null ? doctor.getSpecialization() : "General";
    }

    public String getSnapToken() {
        return snapToken;
    }

    public void setSnapToken(String snapToken) {
        this.snapToken = snapToken;
    }

    public String getMidtransOrderId() {
        return midtransOrderId;
    }

    public void setMidtransOrderId(String midtransOrderId) {
        this.midtransOrderId = midtransOrderId;
    }
}
