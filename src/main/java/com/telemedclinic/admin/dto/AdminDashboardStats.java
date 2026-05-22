package com.telemedclinic.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardStats {

    private long totalDoctor;
    private long totalPharmacist;
    private long totalCustomer;
    private long totalPharmacy;
    private long totalInactiveUser;
}
