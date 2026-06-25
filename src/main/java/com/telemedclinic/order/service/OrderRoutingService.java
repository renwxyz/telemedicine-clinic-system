package com.telemedclinic.order.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.telemedclinic.order.entity.Order;
import com.telemedclinic.order.entity.OrderItem;
import com.telemedclinic.order.entity.OrderStatus;
import com.telemedclinic.pharmacy.internal.entity.Pharmacy;
import com.telemedclinic.pharmacy.internal.entity.InventoryItem;
import com.telemedclinic.pharmacy.internal.repository.PharmacyRepository;
import com.telemedclinic.pharmacy.internal.repository.InventoryItemRepository;
import com.telemedclinic.user.entity.Customer;

@Service
public class OrderRoutingService {

    private final PharmacyRepository pharmacyRepository;
    private final InventoryItemRepository inventoryItemRepository;

    @Autowired
    public OrderRoutingService(PharmacyRepository pharmacyRepository, InventoryItemRepository inventoryItemRepository) {
        this.pharmacyRepository = pharmacyRepository;
        this.inventoryItemRepository = inventoryItemRepository;
    }

    public void assignNearestPharmacy(Order order) {
        Customer customer = order.getCustomer();
        // Fallback to Jakarta coordinates if null
        double custLat = customer.getLatitude() != null ? customer.getLatitude() : -6.2088;
        double custLon = customer.getLongitude() != null ? customer.getLongitude() : 106.8456;

        List<Pharmacy> allPharmacies = pharmacyRepository.findAll();
        Pharmacy nearestPharmacy = null;
        double minDistance = Double.MAX_VALUE;

        for (Pharmacy pharmacy : allPharmacies) {
            if (!pharmacy.isActive() || !pharmacy.isApprovedPartner()) {
                continue;
            }

            boolean hasAllItems = true;
            for (OrderItem item : order.getItems()) {
                Optional<InventoryItem> invItemOpt = inventoryItemRepository.findByPharmacyAndMedicine_Name(pharmacy, item.getMedicineName());
                if (invItemOpt.isEmpty() || invItemOpt.get().getStock() < item.getQuantity()) {
                    hasAllItems = false;
                    break;
                }
            }

            if (hasAllItems) {
                double distance = calculateHaversineDistance(custLat, custLon, pharmacy.getLatitude(), pharmacy.getLongitude());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestPharmacy = pharmacy;
                }
            }
        }

        if (nearestPharmacy != null) {
            order.setPharmacyId(nearestPharmacy.getPharmacyId());
            for (OrderItem item : order.getItems()) {
                item.setPharmacyName(nearestPharmacy.getName());
            }
            order.setStatus(OrderStatus.PENDING);
        } else {
            // MVP fallback: assume there is always a pharmacy, but if logic fails, pick any active one
            for (Pharmacy pharmacy : allPharmacies) {
                 if (pharmacy.isActive() && pharmacy.isApprovedPartner()) {
                     order.setPharmacyId(pharmacy.getPharmacyId());
                     for (OrderItem item : order.getItems()) {
                         item.setPharmacyName(pharmacy.getName());
                     }
                     break;
                 }
            }
            order.setStatus(OrderStatus.PENDING);
        }
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // return distance in km
    }
}
