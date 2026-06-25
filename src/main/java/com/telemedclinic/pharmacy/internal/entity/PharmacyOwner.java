package com.telemedclinic.pharmacy.internal.entity;

import jakarta.persistence.Entity;
import com.telemedclinic.user.entity.User;
import com.telemedclinic.user.entity.Role;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Column;
import java.util.ArrayList;
import java.util.List;

import com.telemedclinic.pharmacy.internal.entity.Pharmacy;

@Entity
public class PharmacyOwner extends User {

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = true)
    private String identityCardNumber;

    @OneToMany(mappedBy = "owner")
    private List<Pharmacy> pharmacies = new ArrayList<>();

    public PharmacyOwner() {
    }

    public PharmacyOwner(
            String name,
            String email,
            String password,
            String phoneNumber,
            String identityCardNumber
    ) {
        super(
                name,
                email,
                password,
                Role.ROLE_PHARMACY_OWNER
        );
        setPhoneNumber(phoneNumber);
        setIdentityCardNumber(identityCardNumber);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be empty.");
        }
        this.phoneNumber = phoneNumber;
    }

    public String getIdentityCardNumber() {
        return identityCardNumber;
    }

    public void setIdentityCardNumber(String identityCardNumber) {
        this.identityCardNumber = identityCardNumber;
    }

    public List<Pharmacy> getPharmacies() {
        return pharmacies;
    }

    public void addPharmacy(Pharmacy pharmacy) {
        pharmacies.add(pharmacy);
        pharmacy.setOwner(this);
    }
}
