package com.telemedclinic.inventory.entity;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

import com.telemedclinic.medicine.entity.Medicine;
import com.telemedclinic.pharmacy.entity.Pharmacy;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {

    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inventoryItemId;

    @ManyToOne
    @JoinColumn(name = "pharmacy_id")
    private Pharmacy pharmacy;

    @ManyToOne
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    private int stock;
    private double price;

    // Constructor overloading
    public InventoryItem(){}

    public InventoryItem(
            Medicine medicine,
            Pharmacy pharmacy,
            int stock,
            double price
    ) {

        this.medicine = Objects.requireNonNull(
                medicine,
                "Medicine cannot be null."
        );

        this.pharmacy = Objects.requireNonNull(
                pharmacy,
                "Pharmacy cannot be null."
        );

        setStock(stock);
        setPrice(price);
    }


    // Getter
    public Long getInventoryItemId() {
        return inventoryItemId;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public Pharmacy getPharmacy() {
        return pharmacy;
    }

    public int getStock() {
        return stock;
    }

    public double getPrice() {
        return price;
    }


    // Setter
    public void setStock(int stock) {

        if (stock < 0) {
            throw new IllegalArgumentException(
                    "Stock cannot be negative."
            );
        }

        this.stock = stock;
    }

    public void setPrice(double price) {

        if (price < 0) {
            throw new IllegalArgumentException(
                    "Price cannot be negative."
            );
        }

        this.price = price;
    }


    // Behavior methods
    public boolean isAvailable(int quantity) {
        return stock >= quantity;
    }

    public boolean isOutOfStock() {
        return stock <= 0;
    }

    public void reduceStock(int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero."
            );
        }

        if (!isAvailable(quantity)) {
            throw new IllegalArgumentException(
                    "Insufficient stock."
            );
        }

        stock -= quantity;
    }

    public void increaseStock(int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero."
            );
        }

        stock += quantity;
    }
}
