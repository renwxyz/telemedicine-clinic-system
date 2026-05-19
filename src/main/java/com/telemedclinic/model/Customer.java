package com.telemedclinic.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;

@Entity
public class Customer extends User {

    // Attributes
    @Column(nullable = false)
    private String address;


    // No-args constructor for JPA
    public Customer() {
    }


    // Constructor
    public Customer(
            String name,
            String email,
            String password,
            String phoneNumber,
            String address
    ) {

        super(
                name,
                email,
                password,
                phoneNumber
        );

        setAddress(address);
    }


    // Getter
    public String getAddress() {
        return address;
    }


    // Setter
    public void setAddress(String address) {

        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException(
                    "Address cannot be empty."
            );
        }

        this.address = address;
    }


    // Behavior methods
    public void updateAddress(String address) {
        setAddress(address);
    }
}
