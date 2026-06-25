package com.telemedclinic.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Transient;
import java.time.LocalDate;
import java.time.Period;

@Entity
public class Customer extends User {

    // Attributes
    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false)
    private Double height;

    @Column(nullable = false)
    private Double weight;

    private Double latitude;
    private Double longitude;

    // No-args constructor for JPA
    public Customer() {
    }


    // Constructor
    public Customer(
            String name,
            String email,
            String password,
            String phoneNumber,
            String address,
            Gender gender,
            LocalDate birthDate,
            Double height,
            Double weight
    ) {

        super(
                name,
                email,
                password,
                Role.ROLE_CUSTOMER
        );

        setPhoneNumber(phoneNumber);
        setAddress(address);
        setGender(gender);
        setBirthDate(birthDate);
        setHeight(height);
        setWeight(weight);
    }


    // Getter
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public Gender getGender() {
        return gender;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public Double getHeight() {
        return height;
    }

    public Double getWeight() {
        return weight;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    @Transient
    public Integer getAge() {
        if (birthDate == null) {
            return null;
        }

        return Period.between(
                birthDate,
                LocalDate.now()
        ).getYears();
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

    public void setAddress(String address) {

        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException(
                    "Address cannot be empty."
            );
        }

        this.address = address;
    }

    public void setGender(Gender gender) {

        if (gender == null) {
            throw new IllegalArgumentException(
                    "Gender cannot be null."
            );
        }

        this.gender = gender;
    }

    public void setBirthDate(LocalDate birthDate) {

        if (birthDate == null) {
            throw new IllegalArgumentException(
                    "Birth date cannot be null."
            );
        }

        if (birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Birth date cannot be in the future."
            );
        }

        this.birthDate = birthDate;
    }

    public void setHeight(Double height) {

        if (height == null || height <= 0) {
            throw new IllegalArgumentException(
                    "Height must be greater than 0."
            );
        }

        this.height = height;
    }

    public void setWeight(Double weight) {

        if (weight == null || weight <= 0) {
            throw new IllegalArgumentException(
                    "Weight must be greater than 0."
            );
        }

        this.weight = weight;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }


    // Behavior methods
    public void updateProfile(
            String name,
            String phoneNumber,
            String address,
            Gender gender,
            LocalDate birthDate,
            Double height,
            Double weight
    ) {

        super.updateProfile(name);
        setPhoneNumber(phoneNumber);
        setAddress(address);
        setGender(gender);
        setBirthDate(birthDate);
        setHeight(height);
        setWeight(weight);
    }
}
