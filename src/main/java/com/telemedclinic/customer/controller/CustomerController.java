package com.telemedclinic.customer.controller;

import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.telemedclinic.user.entity.Role;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    // Menampilkan dashboard sederhana untuk customer yang sudah login.
    @GetMapping("/dashboard")
    public String showDashboard(
            HttpSession session,
            Model model
    ) {

        Object role = session.getAttribute("currentUserRole");

        if (role != Role.ROLE_CUSTOMER) {
            return "redirect:/auth/login";
        }

        String customerName = (String) session.getAttribute("currentUserName");
        String customerEmail = (String) session.getAttribute("currentUserEmail");

        model.addAttribute("customer", Map.of(
                "name", customerName != null ? customerName : "Budi Santoso",
                "email", customerEmail != null ? customerEmail : "budi@gmail.com"
        ));

        model.addAttribute("stats", Map.of(
                "activeConsultations", 2,
                "activeOrders", 1,
                "totalOrders", 8
        ));

        model.addAttribute("recentConsultations", List.of(
                Map.of(
                        "doctorName", "Dr. Ahmad Fauzi",
                        "specialization", "Dokter Umum",
                        "status", "Berlangsung"
                ),
                Map.of(
                        "doctorName", "Dr. Rina Marlina",
                        "specialization", "Penyakit Dalam",
                        "status", "Menunggu"
                )
        ));

        model.addAttribute("recentOrders", List.of(
                Map.of(
                        "id", "ORD-0021",
                        "itemSummary", "Paracetamol 500mg + 2 lainnya",
                        "totalPrice", "Rp 85.000",
                        "status", "Diproses"
                ),
                Map.of(
                        "id", "ORD-0020",
                        "itemSummary", "Vitamin C 500mg",
                        "totalPrice", "Rp 35.000",
                        "status", "Selesai"
                )
        ));

        return "customer/dashboard";
    }
}
