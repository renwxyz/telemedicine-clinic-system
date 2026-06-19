package com.telemedclinic.consultation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.telemedclinic.consultation.model.Consultation;
import com.telemedclinic.consultation.model.ConsultationStatus;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    List<Consultation> findByCustomerUserIdOrderByCreatedAtDesc(Long customerId);
    List<Consultation> findByCustomerUserIdAndStatusOrderByCreatedAtDesc(Long customerId, ConsultationStatus status);
    List<Consultation> findByDoctorUserIdAndStatusOrderByCreatedAtAsc(Long doctorId, ConsultationStatus status);
    List<Consultation> findByDoctorUserIdAndStatusOrderByCreatedAtDesc(Long doctorId, ConsultationStatus status);
    List<Consultation> findByDoctorUserIdAndStatusInOrderByCreatedAtAsc(Long doctorId, java.util.List<ConsultationStatus> statuses);
    boolean existsByCustomer_UserIdAndStatusIn(Long customerId, java.util.List<com.telemedclinic.consultation.model.ConsultationStatus> statuses);
}
