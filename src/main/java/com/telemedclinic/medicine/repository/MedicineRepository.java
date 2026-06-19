package com.telemedclinic.medicine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.telemedclinic.medicine.entity.Medicine;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    java.util.List<Medicine> findByNameContainingIgnoreCase(String name);
}
