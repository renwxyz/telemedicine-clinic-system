package com.telemedclinic.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.telemedclinic.model.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    boolean existsByEmail(String email);
}
