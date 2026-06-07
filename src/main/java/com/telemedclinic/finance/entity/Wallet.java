package com.telemedclinic.finance.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long pharmacyId;

    private Double balance = 0.0;

    public Wallet() {
    }

    public Wallet(Long pharmacyId) {
        this.pharmacyId = pharmacyId;
        this.balance = 0.0;
    }

    public Long getId() {
        return id;
    }

    public Long getPharmacyId() {
        return pharmacyId;
    }

    public Double getBalance() {
        return balance;
    }

    public void setPharmacyId(Long pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public void addBalance(Double amount) {
        this.balance += amount;
    }
}
