package com.telemedclinic.prescription.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.telemedclinic.user.entity.Customer;
import com.telemedclinic.user.entity.Doctor;

@Entity
@Table(name = "prescriptions")
public class Prescription {
    
    @Id
    private String prescriptionId;

    // Relasi: Banyak resep bisa dimiliki oleh 1 Dokter
    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor; 

    // Relasi: Banyak resep bisa dimiliki oleh 1 Customer
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToOne
    @JoinColumn(name = "consultation_id")
    private com.telemedclinic.consultation.model.Consultation consultation;

    // Relasi: 1 Resep punya Banyak Obat (Cascade ALL: Jika resep dihapus, itemnya ikut terhapus)
    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrescriptionItem> items;

    private LocalDateTime issuedDate;
    private Boolean isUsed = false;
    private Boolean isStockAvailable = true;

    // Default constructor wajib untuk JPA (Spring Boot)
    public Prescription() {
    }

    // Constructor bawaanmu (Logika awal tetap aman)
    public Prescription(Doctor doctor, Customer customer) {
        this.prescriptionId = "RX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.doctor = doctor;
        this.customer = customer;
        this.items = new ArrayList<>(); 
        this.issuedDate = LocalDateTime.now();
        this.isUsed = false;
    }

    // --- METHOD UTAMA ---
    public void addItem(PrescriptionItem item) {
        item.setPrescription(this); // Wajib di-set agar nyambung ke database
        this.items.add(item);
    }

    public List<PrescriptionItem> getItems() {
        return items;
    }

    public void setItems(List<PrescriptionItem> items) {
        this.items = items;
    }

    public void setConsultation(com.telemedclinic.consultation.model.Consultation consultation) {
        this.consultation = consultation;
    }

    public com.telemedclinic.consultation.model.Consultation getConsultation() {
        return consultation;
    }

    public boolean isValid() {
        return !isUsed && !items.isEmpty();
    }

    public void markAsUsed() {
        this.isUsed = true;
    }

    // --- GETTER ---
    public String getPrescriptionId() { return prescriptionId; }
    public Doctor getDoctor() { return doctor; }
    public Customer getCustomer() { return customer; }
    public LocalDateTime getIssuedDate() { return issuedDate; }
    public Boolean getIsUsed() { return isUsed; }
    public void setIsUsed(Boolean isUsed) { this.isUsed = isUsed; }
    
    public Boolean getIsStockAvailable() { return isStockAvailable; }
    public void setIsStockAvailable(Boolean isStockAvailable) { this.isStockAvailable = isStockAvailable; }
}