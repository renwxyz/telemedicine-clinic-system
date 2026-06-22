package com.telemedclinic.prescription.service;

import com.telemedclinic.prescription.exception.PrescriptionNotFoundException;
import com.telemedclinic.prescription.model.Prescription;
import com.telemedclinic.prescription.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service // Penanda wajib agar Spring Boot tahu ini adalah "Otak" utama
public class PrescriptionService {

    // Memanggil jembatan database yang tadi kamu buat
    private final PrescriptionRepository prescriptionRepository;

    @Autowired
    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    // 1. Fitur untuk DOKTER: Menyimpan resep baru ke database
    public Prescription createPrescription(Prescription prescription) {
        return prescriptionRepository.save(prescription);
    }

    // 2. Fitur PENJAGA GAWANG: Mencari resep di database
    public Prescription getPrescriptionById(String prescriptionId) {
        // Coba cari resepnya. Kalau tidak ketemu, langsung lempar Exception buatanmu!
        return prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new PrescriptionNotFoundException("Resep dengan ID " + prescriptionId + " tidak ditemukan atau tidak valid!"));
    }

    // 3. Fitur untuk CUSTOMER/APOTEK: Menebus resep
    public Prescription redeemPrescription(String prescriptionId) {
        // Cari resepnya (otomatis melewati penjaga gawang di atas)
        Prescription prescription = getPrescriptionById(prescriptionId);
        
        // Cek apakah resep sudah pernah dipakai
        if (prescription.getIsUsed()) {
            throw new IllegalStateException("Gagal! Resep ini sudah pernah ditebus sebelumnya.");
        }
        
        // Tandai resep sudah dipakai
        prescription.markAsUsed();
        
        // Simpan perubahan statusnya ke database
        return prescriptionRepository.save(prescription);
    }
}