package com.telemedclinic.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.telemedclinic.user.entity.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
}
