package com.telemedclinic.controller;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.telemedclinic.dto.CreateDoctorForm;
import com.telemedclinic.dto.CreatePharmacistForm;
import com.telemedclinic.dto.CreatePharmacyForm;
import com.telemedclinic.service.AdminService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // Menampilkan halaman dashboard admin.
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        // TODO: ganti dengan data dari SecurityContext setelah autentikasi diimplementasikan.
        model.addAttribute("admin", Map.of(
                "name", "Administrator",
                "email", "admin@klinikku.id"
        ));

        // TODO: ganti dengan data dari AdminService setelah backend domain admin selesai dibangun.
        model.addAttribute("stats", Map.of(
                "totalDoctors", 24,
                "totalPharmacists", 12,
                "totalCustomers", 1204,
                "totalPharmacies", 8,
                "totalInactiveUsers", 3
        ));

        model.addAttribute("recentUsers", List.of(
                Map.of(
                        "name", "Dr. Ahmad Fauzi",
                        "email", "ahmad@klinikku.id",
                        "role", "ROLE_DOCTOR"
                ),
                Map.of(
                        "name", "Apt. Sari Dewi",
                        "email", "sari@klinikku.id",
                        "role", "ROLE_PHARMACIST"
                ),
                Map.of(
                        "name", "Budi Santoso",
                        "email", "budi@gmail.com",
                        "role", "ROLE_CUSTOMER"
                ),
                Map.of(
                        "name", "Dr. Rina Marlina",
                        "email", "rina@klinikku.id",
                        "role", "ROLE_DOCTOR"
                ),
                Map.of(
                        "name", "Hendra Wijaya",
                        "email", "hendra@gmail.com",
                        "role", "ROLE_CUSTOMER"
                )
        ));

        model.addAttribute("recentPharmacies", List.of(
                Map.of(
                        "name", "Apotek Sehat Jaya",
                        "address", "Jl. Merdeka No.1, Purwokerto",
                        "phoneNumber", "0281-123456",
                        "isActive", true
                ),
                Map.of(
                        "name", "Apotek Medika Plus",
                        "address", "Jl. Sudirman No.5, Purwokerto",
                        "phoneNumber", "0281-654321",
                        "isActive", true
                ),
                Map.of(
                        "name", "Apotek Husada",
                        "address", "Jl. Veteran No.10, Purwokerto",
                        "phoneNumber", "0281-789012",
                        "isActive", false
                )
        ));

        return "admin/dashboard";
    }

    // Menampilkan semua user dari seluruh role.
    @GetMapping("/users")
    public String showUsers(Model model) {
        model.addAttribute("users", adminService.findAllUsers());
        return "admin/users";
    }

    // Mengaktifkan atau menonaktifkan user tertentu.
    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {

        adminService.toggleUserStatus(id);
        redirectAttributes.addFlashAttribute("successMessage", "Status user berhasil diperbarui.");
        return "redirect:/admin/users";
    }

    // Menampilkan form pembuatan akun doctor.
    @GetMapping("/doctors/create")
    public String showCreateDoctorForm(Model model) {
        model.addAttribute("createDoctorForm", new CreateDoctorForm());
        return "admin/doctors/create";
    }

    // Memproses form pembuatan akun doctor.
    @PostMapping("/doctors/create")
    public String createDoctor(
            @Valid @ModelAttribute("createDoctorForm") CreateDoctorForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {
            return "admin/doctors/create";
        }

        adminService.createDoctor(form);
        redirectAttributes.addFlashAttribute("successMessage", "Akun doctor berhasil dibuat.");
        return "redirect:/admin/users";
    }

    // Menampilkan form pembuatan akun pharmacist.
    @GetMapping("/pharmacists/create")
    public String showCreatePharmacistForm(Model model) {
        model.addAttribute("createPharmacistForm", new CreatePharmacistForm());
        model.addAttribute("pharmacies", adminService.findAllPharmacies());
        return "admin/pharmacists/create";
    }

    // Memproses form pembuatan akun pharmacist.
    @PostMapping("/pharmacists/create")
    public String createPharmacist(
            @Valid @ModelAttribute("createPharmacistForm") CreatePharmacistForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pharmacies", adminService.findAllPharmacies());
            return "admin/pharmacists/create";
        }

        adminService.createPharmacist(form);
        redirectAttributes.addFlashAttribute("successMessage", "Akun pharmacist berhasil dibuat.");
        return "redirect:/admin/users";
    }

    // Menampilkan semua pharmacy yang terdaftar.
    @GetMapping("/pharmacies")
    public String showPharmacies(Model model) {
        model.addAttribute("pharmacies", adminService.findAllPharmacies());
        return "admin/pharmacies";
    }

    // Menampilkan form pembuatan pharmacy.
    @GetMapping("/pharmacies/create")
    public String showCreatePharmacyForm(Model model) {
        model.addAttribute("createPharmacyForm", new CreatePharmacyForm());
        return "admin/pharmacies/create";
    }

    // Memproses form pembuatan pharmacy.
    @PostMapping("/pharmacies/create")
    public String createPharmacy(
            @Valid @ModelAttribute("createPharmacyForm") CreatePharmacyForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {
            return "admin/pharmacies/create";
        }

        adminService.createPharmacy(form);
        redirectAttributes.addFlashAttribute("successMessage", "Pharmacy berhasil dibuat.");
        return "redirect:/admin/pharmacies";
    }
}
