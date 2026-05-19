package com.telemedclinic.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Admin extends User {

    // Attributes
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
                phoneNumber
        );

        setEmployeeNumber(employeeNumber);
    }


    // Getter
    public String getEmployeeNumber() {
        return employeeNumber;
    }


    // Setter
    public void setEmployeeNumber(String employeeNumber) {

        if (employeeNumber == null || employeeNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Employee number cannot be empty."
            );
        }

        this.employeeNumber = employeeNumber;
    }


    // Behavior methods
    public boolean hasEmployeeNumber(String employeeNumber) {

        if (employeeNumber == null) {
            return false;
        }

        return this.employeeNumber.equalsIgnoreCase(employeeNumber);
    }
}
