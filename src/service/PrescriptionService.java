package service;

import domain.Medicine;
import domain.Prescription;
import domain.PrescriptionItem;
import domain.PrescriptionNotFoundException;

public class PrescriptionService {

    /**
     * Mengecek apakah resep masih valid (belum kedaluwarsa/belum dipakai).
     */
    public boolean validatePrescription(Prescription p) throws PrescriptionNotFoundException {
        if (p == null) {
            throw new PrescriptionNotFoundException("Resep tidak valid: Resep tidak ditemukan (null).");
        }
        
        // Memanggil method isValid() yang sudah kamu buat kemarin di class Prescription
        return p.isValid();
    }

    /**
     * Mengecek apakah obat keras yang ingin dibeli benar-benar tercantum di dalam resep dokter.
     */
    public boolean isMedicineInPrescription(Prescription p, Medicine m) throws PrescriptionNotFoundException {
        // 1. Pastikan resepnya valid terlebih dahulu
        if (!validatePrescription(p)) {
            return false;
        }

        // 2. Looping untuk mencari apakah ID obat ada di dalam daftar item resep
        for (PrescriptionItem item : p.getItems()) {
            if (item.getMedicine().getMedicineId().equals(m.getMedicineId())) {
                return true; // Obat ditemukan di dalam resep
            }
        }
        
        // Jika looping selesai dan tidak ditemukan
        return false;
    }

    /**
     * Menandai resep sudah digunakan setelah proses checkout berhasil.
     */
    public void markAsUsed(Prescription p) throws PrescriptionNotFoundException {
        if (p == null) {
            throw new PrescriptionNotFoundException("Gagal menandai resep: Resep tidak ditemukan.");
        }
        
        if (p.isUsed()) {
            throw new IllegalStateException("Gagal: Resep ini sudah pernah digunakan sebelumnya.");
        }
        
        // Memanggil method markAsUsed() dari domain layer
        p.markAsUsed();
    }
}