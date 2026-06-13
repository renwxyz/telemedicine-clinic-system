package com.telemedclinic.pharmacist.controller;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

import com.telemedclinic.user.entity.Pharmacist;
import com.telemedclinic.user.entity.Role;
import com.telemedclinic.user.entity.User;
import com.telemedclinic.user.repository.UserRepository;
import com.telemedclinic.order.repository.OrderRepository;
import com.telemedclinic.inventory.repository.InventoryItemRepository;
import com.telemedclinic.order.entity.Order;
import com.telemedclinic.order.entity.OrderItem;
import com.telemedclinic.order.entity.OrderStatus;
import com.telemedclinic.inventory.entity.InventoryItem;

@Controller
@RequestMapping("/pharmacist")
public class PharmacistController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final InventoryItemRepository inventoryItemRepository;

    public PharmacistController(UserRepository userRepository, OrderRepository orderRepository, InventoryItemRepository inventoryItemRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.inventoryItemRepository = inventoryItemRepository;
    }

    private Optional<Pharmacist> findAuthenticatedPharmacist(HttpSession session) {
        String roleValue = session.getAttribute("currentUserRole") != null
                ? session.getAttribute("currentUserRole").toString()
                : null;

        if (!Role.ROLE_PHARMACIST.name().equals(roleValue)) {
            return Optional.empty();
        }

        String email = (String) session.getAttribute("currentUserEmail");
        if (email == null) {
            return Optional.empty();
        }

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty() || !(optionalUser.get() instanceof Pharmacist pharmacist)) {
            return Optional.empty();
        }

        return Optional.of(pharmacist);
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Optional<Pharmacist> optionalPharmacist = findAuthenticatedPharmacist(session);
        if (optionalPharmacist.isEmpty()) {
            return "redirect:/auth/login";
        }

        Pharmacist pharmacist = optionalPharmacist.get();
        model.addAttribute("pharmacist", pharmacist);
        model.addAttribute("pharmacy", pharmacist.getPharmacy());
        return "pharmacist/pharmacy-dashboard";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        Optional<Pharmacist> optionalPharmacist = findAuthenticatedPharmacist(session);
        if (optionalPharmacist.isEmpty()) {
            return "redirect:/auth/login";
        }

        Pharmacist pharmacist = optionalPharmacist.get();
        model.addAttribute("pharmacist", pharmacist);
        model.addAttribute("pharmacy", pharmacist.getPharmacy());
        return "pharmacist/pharmacy-profile-v2";
    }

    @GetMapping("/validasi")
    public String validasi(HttpSession session, Model model) {
        Optional<Pharmacist> optionalPharmacist = findAuthenticatedPharmacist(session);
        if (optionalPharmacist.isEmpty()) {
            return "redirect:/auth/login";
        }

        model.addAttribute("pharmacist", optionalPharmacist.get());
        return "pharmacist/pharmacy-validasi-resep";
    }

    @GetMapping("/tracking")
    public String tracking(HttpSession session, Model model) {
        Optional<Pharmacist> optionalPharmacist = findAuthenticatedPharmacist(session);
        if (optionalPharmacist.isEmpty()) {
            return "redirect:/auth/login";
        }

        Pharmacist pharmacist = optionalPharmacist.get();
        model.addAttribute("pharmacist", pharmacist);

        if (pharmacist.getPharmacy() != null) {
            java.util.List<Order> orders = orderRepository.findByPharmacyIdOrderByCreatedAtDesc(pharmacist.getPharmacy().getPharmacyId());
            java.util.List<Order> filteredOrders = orders.stream()
                .filter(order -> order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CANCELLED)
                .collect(java.util.stream.Collectors.toList());
            model.addAttribute("orders", filteredOrders);
        } else {
            model.addAttribute("orders", java.util.List.of());
        }

        return "pharmacist/pharmacy-tracking-pesanan";
    }

    @GetMapping("/katalog")
    public String katalog(HttpSession session, Model model) {
        Optional<Pharmacist> optionalPharmacist = findAuthenticatedPharmacist(session);
        if (optionalPharmacist.isEmpty()) {
            return "redirect:/auth/login";
        }

        model.addAttribute("pharmacist", optionalPharmacist.get());
        return "pharmacist/pharmacy-katalog-obat";
    }

    @GetMapping("/keuangan")
    public String keuangan(HttpSession session, Model model) {
        Optional<Pharmacist> optionalPharmacist = findAuthenticatedPharmacist(session);
        if (optionalPharmacist.isEmpty()) {
            return "redirect:/auth/login";
        }

        model.addAttribute("pharmacist", optionalPharmacist.get());
        return "pharmacist/pharmacy-pendapatan";
    }

    @PostMapping("/orders/{id}/ship")
    @Transactional
    public String shipOrder(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Optional<Pharmacist> optionalPharmacist = findAuthenticatedPharmacist(session);
        if (optionalPharmacist.isEmpty()) {
            return "redirect:/auth/login";
        }

        Pharmacist pharmacist = optionalPharmacist.get();
        if (pharmacist.getPharmacy() == null) {
            redirectAttributes.addFlashAttribute("error", "Anda tidak terkait dengan apotek manapun.");
            return "redirect:/pharmacist/dashboard";
        }

        Optional<Order> optionalOrder = orderRepository.findById(id);
        if (optionalOrder.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Pesanan tidak ditemukan.");
            return "redirect:/pharmacist/dashboard";
        }

        Order order = optionalOrder.get();
        
        // Validasi status
        if (order.getStatus() != OrderStatus.PROCESSING) {
            redirectAttributes.addFlashAttribute("error", "Pesanan tidak dalam status PROCESSING.");
            return "redirect:/pharmacist/dashboard";
        }

        // Iterasi pada semua item pesanan
        for (OrderItem item : order.getItems()) {
            // Karena OrderItem hanya menyimpan nama obat, kita gunakan method pencarian berdasarkan nama
            Optional<InventoryItem> optInventory = inventoryItemRepository.findByPharmacyAndMedicine_Name(pharmacist.getPharmacy(), item.getMedicineName());
            
            if (optInventory.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Stok tidak ditemukan untuk obat: " + item.getMedicineName());
                return "redirect:/pharmacist/dashboard";
            }
            
            InventoryItem inventoryItem = optInventory.get();
            if (inventoryItem.getStock() < item.getQuantity()) {
                redirectAttributes.addFlashAttribute("error", "Stok tidak mencukupi untuk obat: " + item.getMedicineName());
                return "redirect:/pharmacist/dashboard";
            }
            
            // Potong stok
            inventoryItem.reduceStock(item.getQuantity());
            inventoryItemRepository.save(inventoryItem);
        }

        // Ubah status order
        order.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(order);

        redirectAttributes.addFlashAttribute("success", "Pesanan berhasil dikirim dan stok telah dipotong!");
        // Redirect ke halaman tracking/orders apoteker
        return "redirect:/pharmacist/tracking";
    }
}
