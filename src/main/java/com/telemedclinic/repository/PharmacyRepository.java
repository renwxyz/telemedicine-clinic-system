package com.telemedclinic.repository;

import com.telemedclinic.model.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PharmacyRepository extends JpaRepository<Pharmacy, Long>{
    
}
