package com.telemedclinic.user.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.telemedclinic.user.entity.Doctor;
import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

  // Method sakti untuk mengambil daftar dokter yang masih aktif
    List<Doctor> findByActiveTrue();

}
