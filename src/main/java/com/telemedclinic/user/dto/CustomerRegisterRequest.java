package com.telemedclinic.user.dto;

import java.time.LocalDate;
import com.telemedclinic.user.entity.Gender;

public class CustomerRegisterRequest {

    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private String address;
    private Gender gender;
    private LocalDate birthDate;
    private Double height;
    private Double weight;

    public CustomerRegisterRequest() {
    }

    public CustomerRegisterRequest(
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

        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.gender = gender;
        this.birthDate = birthDate;
        this.height = height;
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

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

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }
}
