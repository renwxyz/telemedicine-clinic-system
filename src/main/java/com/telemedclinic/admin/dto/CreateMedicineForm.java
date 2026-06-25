package com.telemedclinic.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateMedicineForm {

    @NotBlank(message = "Nama obat tidak boleh kosong")
    private String name;

    @NotBlank(message = "Deskripsi obat tidak boleh kosong")
    private String description;

    @NotBlank(message = "Kategori obat tidak boleh kosong")
    private String category;

    private boolean requiresPrescription;

    private String imageUrl;
}
