package com.telemedclinic.pharmacy.internal.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.telemedclinic.pharmacy.internal.entity.InventoryItem;
import com.telemedclinic.pharmacy.internal.repository.InventoryItemRepository;
import com.telemedclinic.pharmacy.internal.entity.Pharmacy;
import com.telemedclinic.medicine.entity.Medicine;
import com.telemedclinic.pharmacy.api.InventoryQueryApi;
import com.telemedclinic.pharmacy.api.InventoryOperationsApi;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService implements InventoryQueryApi, InventoryOperationsApi {

    private final InventoryItemRepository inventoryItemRepository;

    public InventoryService(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItem> searchAvailableMedicines(String keyword) {
        // Asumsi ada method findByMedicine_NameContainingIgnoreCase di repository
        return inventoryItemRepository.findAll().stream()
                .filter(item -> item.getMedicine().getName().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItem> getAllAvailableMedicines() {
        return inventoryItemRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InventoryItem> getInventoryItem(Long id) {
        return inventoryItemRepository.findById(id);
    }

    @Override
    @Transactional
    public void deductStock(Long inventoryItemId, int quantity) {
        InventoryItem item = inventoryItemRepository.findById(inventoryItemId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory Item not found"));
        item.reduceStock(quantity);
        inventoryItemRepository.save(item);
    }

    @Transactional
    public InventoryItem addOrUpdateInventory(Pharmacy pharmacy, Medicine medicine, int quantityToAdd, double price) {
        Optional<InventoryItem> optionalItem = inventoryItemRepository.findByPharmacyAndMedicine_Name(pharmacy, medicine.getName());

        InventoryItem item;
        if (optionalItem.isPresent()) {
            item = optionalItem.get();
            item.increaseStock(quantityToAdd);
            item.setPrice(price);
        } else {
            item = new InventoryItem(medicine, pharmacy, quantityToAdd, price);
        }

        return inventoryItemRepository.save(item);
    }
    
    @Transactional
    public void updateStock(Long inventoryItemId, int newStock) {
        InventoryItem item = inventoryItemRepository.findById(inventoryItemId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory Item not found"));
        
        item.setStock(newStock);
        inventoryItemRepository.save(item);
    }
}
