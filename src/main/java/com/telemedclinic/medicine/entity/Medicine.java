package com.telemedclinic.medicine.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;


@Entity
@Table(name = "medicines")
public class Medicine {

    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long medicineId;

    private String name;
    private String description;
    private String category;

    private boolean requiresPrescription;
    private String imageUrl;
    // Constructor Overloading
    public Medicine(){}

    public Medicine(
            String name,
            String description,
            String category,
            boolean requiresPrescription,
            String imageUrl
    ) {

        setName(name);
        setDescription(description);
        setCategory(category);
        this.requiresPrescription = requiresPrescription;
        this.imageUrl = imageUrl;
    }

    public Medicine(
            String name,
            String description,
            String category,
            boolean requiresPrescription
    ) {
        this(name, description, category, requiresPrescription, null);
    }


    // Getter
    public Long getMedicineId() {
        return medicineId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public boolean isRequiresPrescription() {
        return requiresPrescription;
    }

    public String getImageUrl() {
        return imageUrl;
    }


    // Setter
    public void setName(String name) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Medicine name cannot be empty."
            );
        }

        this.name = name;
    }

    public void setDescription(String description) {

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Medicine description cannot be empty."
            );
        }

        this.description = description;
    }

    public void setCategory(String category) {

        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException(
                    "Medicine category cannot be empty."
            );
        }

        this.category = category;
    }

    public void setRequiresPrescription(boolean requiresPrescription) {
        this.requiresPrescription = requiresPrescription;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


    // Behavior methods
    public boolean isPrescriptionMedicine() {
        return requiresPrescription;
    }

    public boolean isSameCategory(String category) {

        return this.category.equalsIgnoreCase(category);
    }
}