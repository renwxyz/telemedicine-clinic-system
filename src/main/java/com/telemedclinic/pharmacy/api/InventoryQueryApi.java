package com.telemedclinic.pharmacy.api;

import java.util.List;
import java.util.Optional;
import com.telemedclinic.pharmacy.internal.entity.InventoryItem;

public interface InventoryQueryApi {
    List<InventoryItem> searchAvailableMedicines(String keyword);
    List<InventoryItem> getAllAvailableMedicines();
    Optional<InventoryItem> getInventoryItem(Long id);
    // Transaksi eksternal bisa memotong stok
    void deductStock(Long inventoryItemId, int quantity);
}
