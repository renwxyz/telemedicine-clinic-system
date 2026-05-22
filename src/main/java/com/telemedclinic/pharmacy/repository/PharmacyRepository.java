package com.telemedclinic.pharmacy.repository;

import com.telemedclinic.pharmacy.entity.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PharmacyRepository extends JpaRepository<Pharmacy, Long>{
    
}
