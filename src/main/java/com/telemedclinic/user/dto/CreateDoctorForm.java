package com.telemedclinic.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDoctorForm {

    @NotBlank(message = "Nama dokter tidak boleh kosong.")
    @Size(min = 2, max = 100, message = "Nama dokter harus terdiri dari 2 sampai 100 karakter.")
    private String name;

    @NotBlank(message = "Email dokter tidak boleh kosong.")
    @Email(message = "Format email dokter tidak valid.")
    private String email;

    @NotBlank(message = "Password dokter tidak boleh kosong.")
    @Size(min = 8, message = "Password minimal 8 karakter.")
    private String password;

    @NotBlank(message = "Nomor HP dokter tidak boleh kosong.")
    @Size(min = 8, max = 20, message = "Nomor HP harus terdiri dari 8 sampai 20 karakter.")
    private String phoneNumber;

    @NotBlank(message = "Spesialisasi dokter tidak boleh kosong.")
    @Size(max = 100, message = "Spesialisasi maksimal 100 karakter.")
    private String specialization;

    @NotBlank(message = "Nomor lisensi dokter tidak boleh kosong.")
    @Size(max = 100, message = "Nomor lisensi maksimal 100 karakter.")
    private String licenseNumber;
}
