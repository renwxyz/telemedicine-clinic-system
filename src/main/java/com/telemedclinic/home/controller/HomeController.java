package com.telemedclinic.home.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.telemedclinic.pharmacy.internal.repository.PharmacyRepository;
import com.telemedclinic.pharmacy.internal.entity.Pharmacy;
import com.telemedclinic.user.entity.Role;
import com.telemedclinic.user.entity.Doctor;
import com.telemedclinic.user.repository.DoctorRepository;
import com.telemedclinic.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

import org.springframework.web.bind.annotation.RequestParam;

import com.telemedclinic.pharmacy.api.InventoryQueryApi;
import com.telemedclinic.pharmacy.internal.entity.InventoryItem;

@Controller
public class HomeController {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final PharmacyRepository pharmacyRepository;
    private final InventoryQueryApi inventoryQueryApi;

    public HomeController(DoctorRepository doctorRepository, 
                          UserRepository userRepository, 
                          PharmacyRepository pharmacyRepository,
                          InventoryQueryApi inventoryQueryApi) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.inventoryQueryApi = inventoryQueryApi;
    }

    @GetMapping("/")
    public String index(Model model) {
        long doctorCount = doctorRepository.count();
        long patientCount = userRepository.countByRole(Role.ROLE_CUSTOMER);
        long pharmacyCount = pharmacyRepository.count();

        model.addAttribute("doctorCount", doctorCount > 0 ? doctorCount : 500);
        model.addAttribute("patientCount", patientCount > 0 ? patientCount : 10000);
        model.addAttribute("pharmacyCount", pharmacyCount > 0 ? pharmacyCount : 50);

        List<Doctor> featuredDoctors = doctorRepository.findByActiveTrue()
                .stream().limit(4).collect(Collectors.toList());
        List<Pharmacy> featuredPharmacies = pharmacyRepository.findTop5ByOrderByPharmacyIdDesc()
                .stream().limit(3).collect(Collectors.toList());

        model.addAttribute("featuredDoctors", featuredDoctors);
        model.addAttribute("featuredPharmacies", featuredPharmacies);

        return "index";
    }

    @GetMapping("/katalog-obat")
    public String showPublicCatalog(
            Model model,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String sort
    ) {
        List<InventoryItem> medicines;
        if (search != null && !search.isBlank()) {
            medicines = inventoryQueryApi.searchAvailableMedicines(search);
        } else {
            medicines = inventoryQueryApi.getAllAvailableMedicines();
        }

        if (type != null && !type.isBlank()) {
            if ("BEBAS".equalsIgnoreCase(type)) {
                medicines = medicines.stream()
                        .filter(item -> !item.getMedicine().isRequiresPrescription())
                        .toList();
            } else if ("RESEP".equalsIgnoreCase(type)) {
                medicines = medicines.stream()
                        .filter(item -> item.getMedicine().isRequiresPrescription())
                        .toList();
            }
        }

        if (sort != null) {
            if ("price-asc".equals(sort)) {
                medicines = medicines.stream()
                        .sorted(Comparator.comparingDouble(InventoryItem::getPrice))
                        .toList();
            } else if ("price-desc".equals(sort)) {
                medicines = medicines.stream()
                        .sorted(Comparator.comparingDouble(InventoryItem::getPrice).reversed())
                        .toList();
            } else if ("name-asc".equals(sort)) {
                medicines = medicines.stream()
                        .sorted(Comparator.comparing(item -> item.getMedicine().getName(), String.CASE_INSENSITIVE_ORDER))
                        .toList();
            }
        }

        model.addAttribute("medicines", medicines);
        model.addAttribute("search", search);
        model.addAttribute("type", type);
        model.addAttribute("sort", sort);
        model.addAttribute("totalPages", 1);
        model.addAttribute("currentPage", 1);

        return "public-katalog-obat";
    }
}
