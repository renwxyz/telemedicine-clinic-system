package com.telemedclinic.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Admin extends User {

    // Attributes
    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false, unique = true)
    private String employeeNumber;


    // No-args constructor for JPA
    public Admin() {
    }


    // Constructor
    public Admin(
            String name,
            String email,
            String password,
            String phoneNumber,
            String employeeNumber
    ) {

        super(
                name,
                email,
                password,
                Role.ROLE_ADMIN
        );

        setPhoneNumber(phoneNumber);
        setEmployeeNumber(employeeNumber);
    }


    // Getter
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }


    // Setter
    public void setPhoneNumber(String phoneNumber) {

        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Phone number cannot be empty."
            );
        }

        this.phoneNumber = phoneNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {

        if (employeeNumber == null || employeeNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Employee number cannot be empty."
            );
        }

        this.employeeNumber = employeeNumber;
    }


    // Behavior methods
    public void updateProfile(
            String name,
            String phoneNumber
    ) {

        super.updateProfile(name);
        setPhoneNumber(phoneNumber);
    }

    public boolean hasEmployeeNumber(String employeeNumber) {

        if (employeeNumber == null) {
            return false;
        }

        return this.employeeNumber.equalsIgnoreCase(employeeNumber);
    }
}
