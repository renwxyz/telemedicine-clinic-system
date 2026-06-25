package com.telemedclinic.pharmacy.internal.controller;

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

import com.telemedclinic.pharmacy.internal.entity.Pharmacist;
import com.telemedclinic.user.entity.Role;
import com.telemedclinic.user.entity.User;
import com.telemedclinic.user.repository.UserRepository;
import com.telemedclinic.order.repository.OrderRepository;
import com.telemedclinic.pharmacy.internal.repository.InventoryItemRepository;
import com.telemedclinic.order.entity.Order;
import com.telemedclinic.order.entity.OrderItem;
import com.telemedclinic.order.entity.OrderStatus;
import com.telemedclinic.pharmacy.internal.entity.InventoryItem;

@Controller
@RequestMapping("/pharmacist")
public class PharmacistController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final com.telemedclinic.prescription.repository.PrescriptionRepository prescriptionRepository;
    private final com.telemedclinic.delivery.core.port.out.DeliveryServicePort deliveryServicePort;
    private final com.telemedclinic.medicine.repository.MedicineRepository medicineRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public PharmacistController(UserRepository userRepository, 
                                OrderRepository orderRepository, 
                                InventoryItemRepository inventoryItemRepository, 
                                com.telemedclinic.prescription.repository.PrescriptionRepository prescriptionRepository,
                                com.telemedclinic.delivery.core.port.out.DeliveryServicePort deliveryServicePort,
                                com.telemedclinic.medicine.repository.MedicineRepository medicineRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.deliveryServicePort = deliveryServicePort;
        this.medicineRepository = medicineRepository;
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

        if (pharmacist.getPharmacy() != null) {
            java.util.List<Order> orders = orderRepository.findByPharmacyIdOrderByCreatedAtDesc(pharmacist.getPharmacy().getPharmacyId());
            long pendingCount = orders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
            long processingCount = orders.stream().filter(o -> o.getStatus() == OrderStatus.PROCESSING).count();
            long completedCount = orders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED || o.getStatus() == OrderStatus.SHIPPED).count();
            
            java.util.List<InventoryItem> inventory = inventoryItemRepository.findByPharmacy(pharmacist.getPharmacy());
            long lowStockCount = inventory.stream().filter(i -> i.getStock() <= 10).count();
            
            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("processingCount", processingCount);
            model.addAttribute("completedCount", completedCount);
            model.addAttribute("lowStockCount", lowStockCount);
            
            // Recent orders for dashboard
            java.util.List<Order> recentOrders = orders.stream().limit(5).collect(java.util.stream.Collectors.toList());
            model.addAttribute("recentOrders", recentOrders);
        } else {
            model.addAttribute("pendingCount", 0L);
            model.addAttribute("processingCount", 0L);
            model.addAttribute("completedCount", 0L);
            model.addAttribute("lowStockCount", 0L);
            model.addAttribute("recentOrders", java.util.List.of());
        }

        return "pharmacy/pharmacist/dashboard";
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
        return "pharmacy/pharmacist/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @org.springframework.web.bind.annotation.RequestParam("name") String name,
            @org.springframework.web.bind.annotation.RequestParam("phoneNumber") String phoneNumber,
            HttpSession session, RedirectAttributes redirectAttributes) {
        Optional<Pharmacist> optionalPharmacist = findAuthenticatedPharmacist(session);
        if (optionalPharmacist.isEmpty()) {
            return "redirect:/auth/login";
        }

        Pharmacist pharmacist = optionalPharmacist.get();
        try {
            pharmacist.updateProfile(name, phoneNumber);
            userRepository.save(pharmacist);
            redirectAttributes.addFlashAttribute("success", "Profil berhasil diperbarui.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal memperbarui profil: " + e.getMessage());
        }
        return "redirect:/pharmacist/profile";
    }

    @PostMapping("/profile/password")
    public String updatePassword(
            @org.springframework.web.bind.annotation.RequestParam("currentPassword") String currentPassword,
            @org.springframework.web.bind.annotation.RequestParam("newPassword") String newPassword,
            HttpSession session, RedirectAttributes redirectAttributes) {
        Optional<Pharmacist> optionalPharmacist = findAuthenticatedPharmacist(session);
        if (optionalPharmacist.isEmpty()) {
            return "redirect:/auth/login";
        }

        Pharmacist pharmacist = optionalPharmacist.get();
        if (pharmacist.matchesPassword(currentPassword, passwordEncoder)) {
            pharmacist.changePassword(passwordEncoder.encode(newPassword));
            userRepository.save(pharmacist);
            redirectAttributes.addFlashAttribute("success", "Kata sandi berhasil diperbarui.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Kata sandi saat ini tidak cocok.");
        }
        return "redirect:/pharmacist/profile";
    }

    @GetMapping("/orders")
    public String showOrders(HttpSession session, Model model) {
        Optional<Pharmacist> optionalPharmacist = findAuthenticatedPharmacist(session);
        if (optionalPharmacist.isEmpty()) {
            return "redirect:/auth/login";
        }

        Pharmacist pharmacist = optionalPharmacist.get();
        model.addAttribute("pharmacist", pharmacist);

        if (pharmacist.getPharmacy() != null) {
            java.util.List<Order> orders = orderRepository.findByPharmacyIdOrderByCreatedAtDesc(pharmacist.getPharmacy().getPharmacyId());
            java.util.List<Order> activeOrders = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.PROCESSING)
                .collect(java.util.stream.Collectors.toList());
            model.addAttribute("orders", activeOrders);

            java.util.Map<String, com.telemedclinic.prescription.model.Prescription> prescriptionMap = new java.util.HashMap<>();
            for(Order o : activeOrders) {
                if(o.getPrescriptionId() != null) {
                    java.util.Optional<com.telemedclinic.prescription.model.Prescription> p = prescriptionRepository.findById(o.getPrescriptionId());
                    p.ifPresent(val -> prescriptionMap.put(o.getPrescriptionId(), val));
                }
            }
            model.addAttribute("prescriptionMap", prescriptionMap);

        } else {
            model.addAttribute("orders", java.util.List.of());
        }

        return "pharmacy/pharmacist/orders";
    }

    @PostMapping("/orders/{id}/accept")
    public String acceptOrder(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Optional<Pharmacist> optionalPharmacist = findAuthenticatedPharmacist(session);
        if (optionalPharmacist.isEmpty()) {
            return "redirect:/auth/login";
        }

        Optional<Order> optOrder = orderRepository.findById(id);

        if (optOrder.isPresent()) {
            Order order = optOrder.get();
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.PROCESSING);
                orderRepository.save(order);
                redirectAttributes.addFlashAttribute("success", "Pesanan berhasil diterima dan masuk tahap penyiapan!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Status pesanan tidak valid untuk diterima.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Pesanan tidak ditemukan.");
        }

        return "redirect:/pharmacist/orders";
    }

    @PostMapping("/orders/{id}/reject")
    public String rejectOrder(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Optional<Pharmacist> optionalPharmacist = findAuthenticatedPharmacist(session);
        if (optionalPharmacist.isEmpty()) {
            return "redirect:/auth/login";
        }

        Optional<Order> optOrder = orderRepository.findById(id);

        if (optOrder.isPresent()) {
            Order order = optOrder.get();
            if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.PROCESSING) {
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
                redirectAttributes.addFlashAttribute("success", "Pesanan berhasil ditolak/dibatalkan.");
            }
        }
        return "redirect:/pharmacist/orders";
    }

    @GetMapping("/history")
    public String history(HttpSession session, Model model) {
        Optional<Pharmacist> optionalPharmacist = findAuthenticatedPharmacist(session);
        if (optionalPharmacist.isEmpty()) {
            return "redirect:/auth/login";
        }

        Pharmacist pharmacist = optionalPharmacist.get();
        model.addAttribute("pharmacist", pharmacist);

        if (pharmacist.getPharmacy() != null) {
            java.util.List<Order> orders = orderRepository.findByPharmacyIdOrderByCreatedAtDesc(pharmacist.getPharmacy().getPharmacyId());
            java.util.List<Order> historyOrders = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED)
                .collect(java.util.stream.Collectors.toList());
            model.addAttribute("orders", historyOrders);
        } else {
            model.addAttribute("orders", java.util.List.of());
        }

        return "pharmacy/pharmacist/history";
    }

    @GetMapping("/katalog")
    public String katalog(HttpSession session, Model model) {
        Optional<Pharmacist> optionalPharmacist = findAuthenticatedPharmacist(session);
        if (optionalPharmacist.isEmpty()) {
            return "redirect:/auth/login";
        }

        model.addAttribute("pharmacist", optionalPharmacist.get());
        Pharmacist pharmacist = optionalPharmacist.get();
        
        if (pharmacist.getPharmacy() != null) {
            java.util.List<InventoryItem> inventory = inventoryItemRepository.findByPharmacy(pharmacist.getPharmacy());
            model.addAttribute("inventory", inventory);
        } else {
            model.addAttribute("inventory", java.util.List.of());
        }
        return "pharmacy/pharmacist/katalog-obat";
    }

    @GetMapping("/katalog/tambah")
    public String showAddMedicineForm(HttpSession session, Model model) {
        Optional<Pharmacist> optionalPharmacist = findAuthenticatedPharmacist(session);
        if (optionalPharmacist.isEmpty()) {
            return "redirect:/auth/login";
        }
        
        Pharmacist pharmacist = optionalPharmacist.get();
        if (pharmacist.getPharmacy() == null) {
            return "redirect:/pharmacist/dashboard";
        }
        
        model.addAttribute("pharmacist", pharmacist);
        model.addAttribute("availableMedicines", medicineRepository.findAll());
        return "pharmacy/pharmacist/katalog-obat-form";
    }

    @PostMapping("/katalog/tambah")
    public String addMedicine(
            @org.springframework.web.bind.annotation.RequestParam("medicineId") Long medicineId,
            @org.springframework.web.bind.annotation.RequestParam("price") double price,
            @org.springframework.web.bind.annotation.RequestParam("stock") int stock,
            HttpSession session, RedirectAttributes redirectAttributes) {
            
        Optional<Pharmacist> optionalPharmacist = findAuthenticatedPharmacist(session);
        if (optionalPharmacist.isEmpty()) {
            return "redirect:/auth/login";
        }
        
        Pharmacist pharmacist = optionalPharmacist.get();
        if (pharmacist.getPharmacy() == null) {
            redirectAttributes.addFlashAttribute("error", "Apoteker belum terdaftar pada apotek.");
            return "redirect:/pharmacist/katalog";
        }
        
        try {
            Optional<com.telemedclinic.medicine.entity.Medicine> optMedicine = medicineRepository.findById(medicineId);
            if (optMedicine.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Obat tidak ditemukan dalam sistem (Master Data).");
                return "redirect:/pharmacist/katalog";
            }
            
            com.telemedclinic.medicine.entity.Medicine medicine = optMedicine.get();
            
            Optional<InventoryItem> existingItem = inventoryItemRepository.findByPharmacyAndMedicine(pharmacist.getPharmacy(), medicine);
            if (existingItem.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Obat sudah ada di katalog Anda. Silakan update stok dari halaman utama.");
                return "redirect:/pharmacist/katalog";
            }
            
            InventoryItem newItem = new InventoryItem(medicine, pharmacist.getPharmacy(), stock, price);
            inventoryItemRepository.save(newItem);
            
            redirectAttributes.addFlashAttribute("success", "Obat berhasil ditambahkan ke inventaris apotek!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal menambahkan obat: " + e.getMessage());
        }
        
        return "redirect:/pharmacist/katalog";
    }

    @PostMapping("/katalog/{id}/stok")
    public String updateStock(@PathVariable Long id, @org.springframework.web.bind.annotation.RequestParam("newStock") int newStock, HttpSession session, RedirectAttributes redirectAttributes) {
        Optional<Pharmacist> optionalPharmacist = findAuthenticatedPharmacist(session);
        if (optionalPharmacist.isEmpty()) {
            return "redirect:/auth/login";
        }
        
        Optional<InventoryItem> optItem = inventoryItemRepository.findById(id);
        if (optItem.isPresent()) {
            InventoryItem item = optItem.get();
            try {
                item.setStock(newStock);
                inventoryItemRepository.save(item);
                redirectAttributes.addFlashAttribute("success", "Stok berhasil diperbarui!");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Gagal memperbarui stok: " + e.getMessage());
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Item tidak ditemukan.");
        }
        
        return "redirect:/pharmacist/katalog";
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

        // Panggil DeliveryService untuk mock delivery
        String address = order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "Alamat tidak diketahui";
        String trackingId = deliveryServicePort.requestDelivery(order.getId(), address);
        order.setTrackingId(trackingId);

        // Ubah status order
        order.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(order);

        redirectAttributes.addFlashAttribute("success", "Pesanan berhasil dikirim dan stok telah dipotong!");
        // Redirect ke halaman orders apoteker
        return "redirect:/pharmacist/orders";
    }
}
