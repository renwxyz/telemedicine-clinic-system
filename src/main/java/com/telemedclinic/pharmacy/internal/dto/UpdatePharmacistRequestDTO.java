package com.telemedclinic.pharmacy.internal.dto;

public record UpdatePharmacistRequestDTO(
    String name,
    String phoneNumber,
    String licenseNumber,
    String shiftSchedule
) {}
