package com.telemedclinic.inventory.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.telemedclinic.inventory.entity.InventoryItem;
import com.telemedclinic.medicine.entity.Medicine;
import com.telemedclinic.pharmacy.entity.Pharmacy;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    List<InventoryItem> findByMedicine_NameContainingIgnoreCase(String name);
    List<InventoryItem> findByMedicine_CategoryIgnoreCase(String category);
    List<InventoryItem> findByMedicine_NameContainingIgnoreCaseAndMedicine_CategoryIgnoreCase(String name, String category);

    Optional<InventoryItem> findByPharmacyAndMedicine(Pharmacy pharmacy, Medicine medicine);
    Optional<InventoryItem> findByPharmacyAndMedicine_Name(Pharmacy pharmacy, String medicineName);
}
