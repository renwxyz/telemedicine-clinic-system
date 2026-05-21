package com.telemedclinic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreatePharmacistForm {

    @NotBlank(message = "Nama pharmacist tidak boleh kosong.")
    @Size(min = 2, max = 100, message = "Nama pharmacist harus terdiri dari 2 sampai 100 karakter.")
    private String name;

    @NotBlank(message = "Email pharmacist tidak boleh kosong.")
    @Email(message = "Format email pharmacist tidak valid.")
    private String email;

    @NotBlank(message = "Password pharmacist tidak boleh kosong.")
    @Size(min = 8, message = "Password minimal 8 karakter.")
    private String password;

    @NotBlank(message = "Nomor HP pharmacist tidak boleh kosong.")
    @Size(min = 8, max = 20, message = "Nomor HP harus terdiri dari 8 sampai 20 karakter.")
    private String phoneNumber;

    @NotBlank(message = "Nomor lisensi pharmacist tidak boleh kosong.")
    @Size(max = 100, message = "Nomor lisensi maksimal 100 karakter.")
    private String licenseNumber;

    @NotNull(message = "Pharmacy harus dipilih.")
    private Long pharmacyId;
}
