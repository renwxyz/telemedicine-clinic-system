package com.telemedclinic.pharmacy.internal.dto;

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
public class CreatePharmacyForm {

    @NotBlank(message = "Nama pharmacy tidak boleh kosong.")
    @Size(min = 2, max = 100, message = "Nama pharmacy harus terdiri dari 2 sampai 100 karakter.")
    private String name;

    @NotBlank(message = "Alamat pharmacy tidak boleh kosong.")
    @Size(min = 5, max = 255, message = "Alamat harus terdiri dari 5 sampai 255 karakter.")
    private String address;

    @NotBlank(message = "Nomor HP pharmacy tidak boleh kosong.")
    @Size(min = 8, max = 20, message = "Nomor HP harus terdiri dari 8 sampai 20 karakter.")
    private String phoneNumber;

    @NotBlank(message = "Nomor dokumen legal tidak boleh kosong.")
    @Size(max = 100, message = "Nomor dokumen legal maksimal 100 karakter.")
    private String legalDocumentNumber;

    @NotNull(message = "Latitude tidak boleh kosong.")
    private Double latitude;

    @NotNull(message = "Longitude tidak boleh kosong.")
    private Double longitude;

    @NotBlank(message = "Nama pemilik tidak boleh kosong.")
    @Size(min = 2, max = 100, message = "Nama pemilik harus terdiri dari 2 sampai 100 karakter.")
    private String ownerName;

    @NotBlank(message = "Email pemilik tidak boleh kosong.")
    @jakarta.validation.constraints.Email(message = "Format email tidak valid.")
    private String ownerEmail;

    @NotBlank(message = "Nomor HP pemilik tidak boleh kosong.")
    @Size(min = 8, max = 20, message = "Nomor HP pemilik harus terdiri dari 8 sampai 20 karakter.")
    private String ownerPhoneNumber;
}
