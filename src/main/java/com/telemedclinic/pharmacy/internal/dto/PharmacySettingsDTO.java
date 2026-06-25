package com.telemedclinic.pharmacy.internal.dto;

public record PharmacySettingsDTO(
    String name,
    String address,
    String phoneNumber,
    String legalDocumentNumber,
    double latitude,
    double longitude
) {}
