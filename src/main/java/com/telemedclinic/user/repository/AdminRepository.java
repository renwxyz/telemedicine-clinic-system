package com.telemedclinic.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.telemedclinic.user.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    boolean existsByEmail(String email);
}
