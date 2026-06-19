package com.telemedclinic.prescription.repository;

import com.telemedclinic.prescription.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, String> {

    long countByCustomerUserId(Long customerId);

    long countByCustomerUserIdAndIsUsedFalse(Long customerId);

    List<Prescription> findTop3ByCustomerUserIdOrderByIssuedDateDesc(Long customerId);

    java.util.Optional<Prescription> findByConsultationId(Long consultationId);
}
