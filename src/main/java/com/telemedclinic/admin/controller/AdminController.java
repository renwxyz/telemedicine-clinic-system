package com.telemedclinic.admin.controller;

import jakarta.validation.Valid;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.telemedclinic.user.dto.CreateDoctorForm;
import com.telemedclinic.user.entity.User;
import com.telemedclinic.pharmacy.internal.dto.CreatePharmacyForm;
import com.telemedclinic.admin.service.AdminService;
import com.telemedclinic.auth.service.DoctorProvisioningResult;
import com.telemedclinic.auth.service.OwnerProvisioningResult;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @ModelAttribute("admin")
    public Map<String, String> adminProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String adminName = "Administrator";
        String adminEmail = "admin@klinikku.id";

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof User user) {
                adminName = user.getName();
                adminEmail = user.getEmail();
            } else if (principal instanceof UserDetails userDetails) {
                adminName = userDetails.getUsername();
                adminEmail = userDetails.getUsername();
            } else if (principal instanceof String username && !"anonymousUser".equals(username)) {
                adminName = username;
                adminEmail = username;
            }
        }

        return Map.of(
                "name", adminName,
                "email", adminEmail
        );
    }

    // Menampilkan halaman dashboard admin.
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("stats", adminService.getDashboardStats());
        model.addAttribute("recentUsers", adminService.findRecentMedicalStaffUsers());
        model.addAttribute("recentPharmacies", adminService.findRecentPharmacies());

        return "admin/dashboard";
    }

    // Menampilkan semua user dari seluruh role.
    @GetMapping("/users")
    public String showUsers(Model model) {
        model.addAttribute("users", adminService.findAllUsers());
        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", 1);
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
        return "admin/create-doctor";
    }

    // Memproses form pembuatan akun doctor.
    @PostMapping("/doctors/create")
    public String createDoctor(
            @Valid @ModelAttribute("createDoctorForm") CreateDoctorForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {
            return "admin/create-doctor";
        }

        try {
            DoctorProvisioningResult result = adminService.createDoctor(form);

            if (result.isEmailSent()) {
                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "Akun dokter berhasil dibuat. Kredensial telah dikirim ke " + form.getEmail() + "."
                );
            } else {
                redirectAttributes.addFlashAttribute(
                        "warningMessage",
                        "Akun dokter berhasil dibuat, tetapi email kredensial gagal dikirim. Gunakan tombol Kirim Ulang."
                );
            }

            return "redirect:/admin/users";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/doctors/create";
        }
    }

    @PostMapping("/doctors/{id}/resend-credentials")
    public String resendDoctorCredentials(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {

        try {
            DoctorProvisioningResult result = adminService.resendDoctorCredentials(id);

            if (result.isEmailSent()) {
                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "Kredensial berhasil dikirim ulang."
                );
            } else {
                redirectAttributes.addFlashAttribute(
                        "warningMessage",
                        "Gagal mengirim ulang kredensial. Silakan coba lagi."
                );
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/users";
    }


    // Menampilkan semua pharmacy yang terdaftar.
    @GetMapping("/pharmacies")
    public String showPharmacies(
            @RequestParam(value = "search", required = false) String search,
            Model model
    ) {

        model.addAttribute("pharmacies", adminService.findPharmacies(search));
        model.addAttribute("search", search);
        return "admin/pharmacies";
    }

    // Menampilkan form pembuatan pharmacy.
    @GetMapping("/pharmacies/create")
    public String showCreatePharmacyForm(Model model) {
        model.addAttribute("createPharmacyForm", new CreatePharmacyForm());
        return "admin/create-pharmacy";
    }

    // Memproses form pembuatan pharmacy.
    @PostMapping("/pharmacies/create")
    public String createPharmacy(
            @Valid @ModelAttribute("createPharmacyForm") CreatePharmacyForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {
            return "admin/create-pharmacy";
        }

        try {
            OwnerProvisioningResult result = adminService.createPharmacy(form);

            if (result.isEmailSent()) {
                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "Pharmacy dan akun pemilik berhasil dibuat. Kredensial telah dikirim ke " + form.getOwnerEmail() + "."
                );
            } else {
                redirectAttributes.addFlashAttribute(
                        "warningMessage",
                        "Pharmacy berhasil dibuat, tetapi email kredensial pemilik gagal dikirim."
                );
            }

            return "redirect:/admin/pharmacies";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/admin/pharmacies/create";
        }
    }

    // Menampilkan semua master data obat.
    @GetMapping("/medicines")
    public String showMedicines(
            @RequestParam(value = "search", required = false) String search,
            Model model
    ) {
        model.addAttribute("medicines", adminService.findMedicines(search));
        model.addAttribute("search", search);
        return "admin/medicines";
    }

    // Menampilkan form pembuatan master data obat.
    @GetMapping("/medicines/create")
    public String showCreateMedicineForm(Model model) {
        model.addAttribute("createMedicineForm", new com.telemedclinic.admin.dto.CreateMedicineForm());
        return "admin/create-medicine";
    }

    // Memproses form pembuatan master data obat.
    @PostMapping("/medicines/create")
    public String createMedicine(
            @Valid @ModelAttribute("createMedicineForm") com.telemedclinic.admin.dto.CreateMedicineForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {
            return "admin/create-medicine";
        }

        try {
            adminService.createMedicine(form);
            redirectAttributes.addFlashAttribute("successMessage", "Obat baru berhasil ditambahkan ke Master Data.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal menambahkan obat: " + e.getMessage());
            return "redirect:/admin/medicines/create";
        }

        return "redirect:/admin/medicines";
    }
}
