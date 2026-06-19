package com.telemedclinic.prescription.model;

import jakarta.persistence.*;
import com.telemedclinic.medicine.entity.Medicine;

@Entity
@Table(name = "prescription_items")
public class PrescriptionItem {
    
    // Spring Boot wajib punya ID unik berupa angka untuk tiap baris data
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relasi balik ke Prescription
    @ManyToOne
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    // Relasi: 1 Item Resep mengacu ke 1 Obat di database
    @ManyToOne
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    private int quantity;
    private String dosage;
    private String instructions;

    // Default constructor wajib untuk JPA
    public PrescriptionItem() {}

    // Constructor bawaanmu
    public PrescriptionItem(Medicine medicine, int quantity, String dosage, String instructions) {
        this.medicine = medicine;
        this.quantity = quantity;
        this.dosage = dosage;
        this.instructions = instructions;
    }

    // Setter untuk menyambungkan ke resep (dipanggil dari dalam Prescription.java)
    public void setPrescription(Prescription prescription) {
        this.prescription = prescription;
    }

    // --- GETTER & SETTER ---
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public Long getId() { return id; }
    public Prescription getPrescription() { return prescription; }
    public Medicine getMedicine() { return medicine; }
    public int getQuantity() { return quantity; }
    public String getDosage() { return dosage; }
    public String getInstructions() { return instructions; }
}