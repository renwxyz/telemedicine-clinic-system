package com.telemedclinic.config.seeder;

import java.util.List;

import org.springframework.stereotype.Component;

import com.telemedclinic.medicine.entity.Medicine;
import com.telemedclinic.medicine.repository.MedicineRepository;
import com.telemedclinic.pharmacy.internal.entity.InventoryItem;
import com.telemedclinic.pharmacy.internal.repository.InventoryItemRepository;
import com.telemedclinic.pharmacy.internal.repository.PharmacyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MedicineSeeder {
    private final MedicineRepository medicineRepository;
    private final PharmacyRepository pharmacyRepository;
    private final InventoryItemRepository inventoryItemRepository;

    public void seed() {
        if (medicineRepository.count() == 0) {
            Medicine paracetamol = new Medicine("Paracetamol 500mg", "Obat penurun panas dan pereda nyeri ringan hingga sedang.", "Analgesik", false, "https://placehold.co/400x400/FFECD1/FF9800?text=Paracetamol");
            Medicine vitaminC = new Medicine("Vitamin C 1000mg", "Suplemen vitamin C untuk menjaga daya tahan tubuh.", "Vitamin & Suplemen", false, "https://placehold.co/400x400/E8F5E9/4CAF50?text=Vitamin+C");
            Medicine amoxicillin = new Medicine("Amoxicillin 500mg", "Antibiotik penisilin yang digunakan untuk mengobati berbagai macam infeksi bakteri.", "Antibiotik", true, "https://placehold.co/400x400/FFEBEE/F44336?text=Amoxicillin");
            Medicine ibuprofen = new Medicine("Ibuprofen 400mg", "Obat pereda nyeri dan peradangan seperti sakit gigi, nyeri haid, dan radang sendi.", "Analgesik / NSAID", true, "https://placehold.co/400x400/E3F2FD/2196F3?text=Ibuprofen");
            Medicine cetirizine = new Medicine("Cetirizine 10mg", "Obat antihistamin untuk meredakan gejala alergi seperti gatal, bersin, dan pilek.", "Antihistamin", false, "https://placehold.co/400x400/F3E5F5/9C27B0?text=Cetirizine");
            Medicine omeprazole = new Medicine("Omeprazole 20mg", "Obat untuk mengatasi masalah lambung seperti asam lambung (GERD) dan tukak lambung.", "Obat Maag", true, "https://placehold.co/400x400/FFF3E0/FF9800?text=Omeprazole");
            Medicine promag = new Medicine("Promag Tablet", "Obat antasida untuk meredakan sakit maag dan perut kembung secara cepat.", "Obat Maag", false, "https://placehold.co/400x400/E0F7FA/00BCD4?text=Promag");
            Medicine tolakAngin = new Medicine("Tolak Angin Cair 15ml", "Obat herbal untuk masuk angin, perut kembung, dan meningkatkan daya tahan tubuh.", "Herbal", false, "https://placehold.co/400x400/FFF9C4/FBC02D?text=Tolak+Angin");
            Medicine betadine = new Medicine("Betadine Antiseptic 15ml", "Cairan antiseptik untuk membersihkan dan mengobati luka agar tidak infeksi.", "Antiseptik / P3K", false, "https://placehold.co/400x400/FFEBEE/D32F2F?text=Betadine");
            Medicine dexamethasone = new Medicine("Dexamethasone 0.5mg", "Obat kortikosteroid untuk mengatasi peradangan hebat dan reaksi alergi parah.", "Kortikosteroid", true, "https://placehold.co/400x400/EFEBE9/795548?text=Dexamethasone");
            Medicine diapet = new Medicine("Diapet Kapsul", "Obat herbal untuk mengatasi diare, memadatkan feses, dan mengurangi mules.", "Obat Diare", false, "https://placehold.co/400x400/E8F5E9/388E3C?text=Diapet");
            Medicine sangobion = new Medicine("Sangobion Kapsul", "Suplemen zat besi dan vitamin untuk mengatasi anemia (kurang darah).", "Vitamin & Suplemen", false, "https://placehold.co/400x400/FCE4EC/E91E63?text=Sangobion");

            medicineRepository.saveAll(List.of(
                    paracetamol, vitaminC, amoxicillin, ibuprofen, cetirizine, 
                    omeprazole, promag, tolakAngin, betadine, dexamethasone, 
                    diapet, sangobion
            ));
            log.info("Medicines berhasil dibuat");
        }

        List<Medicine> medicines = medicineRepository.findAll();
        List<com.telemedclinic.pharmacy.internal.entity.Pharmacy> pharmacies = pharmacyRepository.findAll();

        for (com.telemedclinic.pharmacy.internal.entity.Pharmacy pharmacy : pharmacies) {
            for (Medicine medicine : medicines) {
                if (inventoryItemRepository.findByPharmacyAndMedicine(pharmacy, medicine).isEmpty()) {
                    double dummyPrice = 10000.0 + (medicine.getName().length() * 1500.0);
                    dummyPrice = Math.round(dummyPrice / 500.0) * 500.0;

                    InventoryItem item = new InventoryItem(medicine, pharmacy, 100, dummyPrice);
                    inventoryItemRepository.save(item);
                }
            }
        }
        log.info("Inventory items berhasil didistribusikan ke semua apotek");
    }
}
