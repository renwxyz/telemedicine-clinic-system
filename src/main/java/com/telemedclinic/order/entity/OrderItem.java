package com.telemedclinic.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private String medicineName;

    @Column(nullable = false)
    private String pharmacyName;

    private int quantity;
    private double price;
    private boolean requiresPrescription;
    private double subtotal;

    public OrderItem() {}

    public OrderItem(String medicineName, String pharmacyName, int quantity, double price, boolean requiresPrescription) {
        this.medicineName = medicineName;
        this.pharmacyName = pharmacyName;
        this.quantity = quantity;
        this.price = price;
        this.requiresPrescription = requiresPrescription;
        this.subtotal = quantity * price;
    }

    public Long getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public String getPharmacyName() {
        return pharmacyName;
    }

    public void setPharmacyName(String pharmacyName) {
        this.pharmacyName = pharmacyName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public boolean isRequiresPrescription() {
        return requiresPrescription;
    }

    public double getSubtotal() {
        return subtotal;
    }
}
