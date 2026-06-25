package com.telemedclinic.pharmacy.internal.repository;

import com.telemedclinic.pharmacy.internal.entity.PharmacyOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import com.telemedclinic.pharmacy.internal.entity.PharmacyOwner;
import org.springframework.stereotype.Repository;

@Repository
public interface PharmacyOwnerRepository extends JpaRepository<PharmacyOwner, Long> {
}
