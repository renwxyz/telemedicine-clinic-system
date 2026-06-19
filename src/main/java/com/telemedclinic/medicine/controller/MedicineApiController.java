package com.telemedclinic.medicine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.telemedclinic.medicine.repository.MedicineRepository;

@RestController
@RequestMapping("/api/medicines")
public class MedicineApiController {
    @Autowired
    private MedicineRepository medicineRepository;

    @GetMapping("/search")
    public ResponseEntity<?> searchMedicines(@RequestParam String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
        return ResponseEntity.ok(medicineRepository.findByNameContainingIgnoreCase(query));
    }
}
