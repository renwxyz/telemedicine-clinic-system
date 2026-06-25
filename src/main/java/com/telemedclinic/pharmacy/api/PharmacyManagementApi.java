package com.telemedclinic.pharmacy.api;

import com.telemedclinic.pharmacy.internal.dto.OwnerProfileDTO;
import com.telemedclinic.pharmacy.internal.dto.PharmacySettingsDTO;

import com.telemedclinic.pharmacy.internal.dto.CreatePharmacistRequestDTO;
import com.telemedclinic.pharmacy.internal.dto.WithdrawalRequestDTO;

public interface PharmacyManagementApi {
    void updateOwnerProfile(Long ownerId, OwnerProfileDTO profileDTO);
    void updatePharmacySettings(Long pharmacyId, PharmacySettingsDTO settingsDTO);
    void registerPharmacist(Long pharmacyId, CreatePharmacistRequestDTO dto);
    void requestWithdrawal(Long pharmacyId, WithdrawalRequestDTO dto);
    double getTodayEarnings(Long pharmacyId);
    long getTodayCompletedTransactionsCount(Long pharmacyId);
    void updatePharmacist(Long pharmacistId, com.telemedclinic.pharmacy.internal.dto.UpdatePharmacistRequestDTO dto);
    java.util.List<com.telemedclinic.order.entity.Order> getRecentOrders(Long pharmacyId);
}




