package com.telemedclinic.prescription.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.telemedclinic.user.entity.Customer;
import com.telemedclinic.user.entity.Doctor;

public class Prescription {
    private final String prescriptionId;
    private Doctor doctor; 
    private Customer customer;
    private List<PrescriptionItem> items;
    private LocalDateTime issuedDate;
    private boolean isUsed;

    // Constructor
    public Prescription(Doctor doctor, Customer customer) {
        // Auto-generate ID unik
        this.prescriptionId = "RX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.doctor = doctor;
        this.customer = customer;
        
        // Relasi Komposisi: List item diinisialisasi bersamaan dengan pembuatan resep
        this.items = new ArrayList<>(); 
        
        this.issuedDate = LocalDateTime.now();
        this.isUsed = false; // Resep baru pasti belum digunakan
    }

    // --- METHOD UTAMA SESUAI RANCANGAN ---

    // Menambah obat ke dalam resep
    public void addItem(PrescriptionItem item) {
        this.items.add(item);
    }

    // Mengembalikan daftar obat dalam resep
    public List<PrescriptionItem> getItems() {
        return items;
    }

    // Cek apakah resep masih valid (belum dipakai dan ada isinya)
    public boolean isValid() {
        return !isUsed && !items.isEmpty();
    }

    // Menandai resep sudah dipakai setelah checkout berhasil
    public void markAsUsed() {
        this.isUsed = true;
    }

    // --- GETTER LAINNYA ---
    public String getPrescriptionId() { return prescriptionId; }
    public Doctor getDoctor() { return doctor; }
    public Customer getCustomer() { return customer; }
    public LocalDateTime getIssuedDate() { return issuedDate; }
    public boolean isUsed() { return isUsed; }
}
