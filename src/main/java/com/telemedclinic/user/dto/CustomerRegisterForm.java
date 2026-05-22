package com.telemedclinic.user.dto;

import java.time.LocalDate;
import com.telemedclinic.user.entity.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CustomerRegisterForm {

    @NotBlank(message = "Nama tidak boleh kosong.")
    @Size(min = 2, max = 100, message = "Nama harus terdiri dari 2 sampai 100 karakter.")
    private String name;

    @NotBlank(message = "Email tidak boleh kosong.")
    @Email(message = "Format email tidak valid.")
    private String email;

    @NotBlank(message = "Password tidak boleh kosong.")
    @Size(min = 8, message = "Password minimal 8 karakter.")
    private String password;

    @NotBlank(message = "Nomor HP tidak boleh kosong.")
    @Size(min = 8, max = 20, message = "Nomor HP harus terdiri dari 8 sampai 20 karakter.")
    private String phoneNumber;

    @NotBlank(message = "Alamat tidak boleh kosong.")
    @Size(min = 5, max = 255, message = "Alamat harus terdiri dari 5 sampai 255 karakter.")
    private String address;

    @NotNull(message = "Jenis kelamin harus dipilih.")
    private Gender gender;

    @NotNull(message = "Tanggal lahir tidak boleh kosong.")
    @Past(message = "Tanggal lahir harus berada di masa lalu.")
    private LocalDate birthDate;

    @NotNull(message = "Tinggi badan tidak boleh kosong.")
    @Positive(message = "Tinggi badan harus lebih dari 0.")
    private Double height;

    @NotNull(message = "Berat badan tidak boleh kosong.")
    @Positive(message = "Berat badan harus lebih dari 0.")
    private Double weight;
}
