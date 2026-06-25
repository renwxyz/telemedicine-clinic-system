package com.telemedclinic.pharmacy.internal.dto;

public record CreatePharmacistRequestDTO(
    String name,
    String email,
    String phoneNumber,
    String licenseNumber,
    String shiftSchedule
) {}
