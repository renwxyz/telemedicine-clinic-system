package com.telemedclinic.model;

import java.util.List;
import java.util.ArrayList;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;


@Entity
@Table(name = "pharmacies")
public class Pharmacy {

    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pharmacyId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String legalDocumentNumber;

    private double latitude;
    private double longitude;

    @Column(nullable = false)
    private boolean isActive;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PartnerApplicationStatus applicationStatus;

    @OneToMany(mappedBy = "pharmacy")
    private List<InventoryItem> inventoryItems = new ArrayList<>();


    // Constructor overloading
    public Pharmacy() {}

    public Pharmacy(
            String name,
            String address,
            String phoneNumber,
            String legalDocumentNumber,
            double latitude,
            double longitude
    ) {

        setName(name);
        setAddress(address);
        setPhoneNumber(phoneNumber);
        setLegalDocumentNumber(legalDocumentNumber);
        setLatitude(latitude);
        setLongitude(longitude);

        this.isActive = true;
        this.applicationStatus = PartnerApplicationStatus.APPROVED;

        this.inventoryItems = new ArrayList<>();
    }


    // Getter
    public Long getPharmacyId() {
        return pharmacyId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getLegalDocumentNumber() {
        return legalDocumentNumber;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean isActive() {
        return isActive;
    }

    public PartnerApplicationStatus getApplicationStatus() {
        return applicationStatus;
    }

    public List<InventoryItem> getInventoryItems() {
        return inventoryItems;
    }


    // Setter
    public void setName(String name) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Pharmacy name cannot be empty."
            );
        }

        this.name = name;
    }

    public void setAddress(String address) {

        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException(
                    "Pharmacy address cannot be empty."
            );
        }

        this.address = address;
    }

    public void setPhoneNumber(String phoneNumber) {

        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Pharmacy phone number cannot be empty."
            );
        }

        this.phoneNumber = phoneNumber;
    }

    public void setLegalDocumentNumber(String legalDocumentNumber) {

        if (legalDocumentNumber == null || legalDocumentNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Legal document number cannot be empty."
            );
        }

        this.legalDocumentNumber = legalDocumentNumber;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setApplicationStatus(PartnerApplicationStatus applicationStatus) {

        if (applicationStatus == null) {
            throw new IllegalArgumentException(
                    "Application status cannot be null."
            );
        }

        this.applicationStatus = applicationStatus;
    }


    // Behavior methods
    public void addInventoryItem(InventoryItem inventoryItem) {
        inventoryItems.add(inventoryItem);
    }

    public void removeInventoryItem(InventoryItem inventoryItem) {
        inventoryItems.remove(inventoryItem);
    }

    public boolean hasMedicine(Medicine medicine) {

        for (InventoryItem item : inventoryItems) {

            if (item.getMedicine().equals(medicine)) {
                return true;
            }

        }

        return false;
    }

    public InventoryItem findInventoryItem(Medicine medicine) {

        for (InventoryItem item : inventoryItems) {

            if (item.getMedicine().equals(medicine)) {
                return item;
            }

        }

        return null;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void approveApplication() {
        setApplicationStatus(PartnerApplicationStatus.APPROVED);
        activate();
    }

    public void declineApplication() {
        setApplicationStatus(PartnerApplicationStatus.DECLINED);
        deactivate();
    }

    public boolean isApprovedPartner() {
        return PartnerApplicationStatus.APPROVED.equals(applicationStatus);
    }
}
