package com.telemedclinic.pharmacy.internal.dto;

public record WithdrawalRequestDTO(
    double amount,
    String bankDetails
) {}
