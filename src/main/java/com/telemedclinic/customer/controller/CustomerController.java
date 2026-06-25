package com.telemedclinic.customer.controller;

import jakarta.servlet.http.HttpSession;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
 
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Base64;
import java.time.LocalDate;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.telemedclinic.user.entity.Gender;
import com.telemedclinic.cart.entity.CartItem;
import com.telemedclinic.cart.repository.CartItemRepository;
import com.telemedclinic.consultation.model.Consultation;
import com.telemedclinic.consultation.model.ConsultationStatus;
import com.telemedclinic.consultation.repository.ConsultationRepository;
import com.telemedclinic.customer.dto.CheckoutForm;
import com.telemedclinic.customer.dto.ConsultationForm;
import com.telemedclinic.pharmacy.internal.entity.InventoryItem;
import com.telemedclinic.pharmacy.api.InventoryQueryApi;
import com.telemedclinic.order.entity.DeliveryMethod;
import com.telemedclinic.order.entity.Order;
import com.telemedclinic.order.entity.OrderItem;
import com.telemedclinic.order.entity.OrderStatus;
import com.telemedclinic.order.entity.PaymentMethod;
import com.telemedclinic.order.entity.PaymentStatus;
import com.telemedclinic.order.repository.OrderRepository;
import com.telemedclinic.payment.service.MidtransService;
import com.telemedclinic.prescription.model.Prescription;
import com.telemedclinic.prescription.repository.PrescriptionRepository;
import com.telemedclinic.user.entity.Customer;
import com.telemedclinic.user.entity.Doctor;
import com.telemedclinic.user.entity.Role;
import com.telemedclinic.user.entity.User;
import com.telemedclinic.user.repository.DoctorRepository;
import com.telemedclinic.user.repository.UserRepository;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final InventoryQueryApi inventoryQueryApi;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final ConsultationRepository consultationRepository;

    @Autowired
    private MidtransService midtransService;

    @Autowired
    private com.telemedclinic.consultation.repository.ChatMessageRepository chatMessageRepository;

    @Value("${midtrans.client.key:}")
    private String midtransClientKey;

    @Value("${midtrans.server.key:}")
    private String midtransServerKey;

    public CustomerController(
            UserRepository userRepository,
            DoctorRepository doctorRepository,
            PrescriptionRepository prescriptionRepository,
            InventoryQueryApi inventoryQueryApi,
            CartItemRepository cartItemRepository,
            OrderRepository orderRepository,
            ConsultationRepository consultationRepository
    ) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.inventoryQueryApi = inventoryQueryApi;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.consultationRepository = consultationRepository;
    }

    @ModelAttribute
    public void addCommonAttributes(HttpSession session, Model model) {
        findAuthenticatedCustomer(session).ifPresent(customer -> {
            model.addAttribute("customer", customer);
            model.addAttribute("cartCount", cartItemRepository.countByCustomerUserId(customer.getUserId()));
            model.addAttribute("midtransClientKey", midtransClientKey);
        });
    }

    private void setActiveSection(Model model, String activeSection) {
        model.addAttribute("activeSection", activeSection);
    }

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        Optional<Customer> optionalCustomer = findAuthenticatedCustomer(session);
        if (optionalCustomer.isEmpty()) {
            return "redirect:/auth/login";
        }

        Customer customer = optionalCustomer.get();
        long customerId = customer.getUserId();

        List<Consultation> consultations = consultationRepository.findByCustomerUserIdOrderByCreatedAtDesc(customerId);
        List<Order> orders = orderRepository.findByCustomerUserIdOrderByCreatedAtDesc(customerId);
        long totalPrescriptions = prescriptionRepository.countByCustomerUserId(customerId);
        long unusedPrescriptions = prescriptionRepository.countByCustomerUserIdAndIsUsedFalse(customerId);
        List<Prescription> recentPrescriptions = prescriptionRepository.findTop3ByCustomerUserIdOrderByIssuedDateDesc(customerId);

        long activeConsultations = consultations.stream()
                .filter(c -> c.getStatus().name().equals("PENDING") || c.getStatus().name().equals("IN_PROGRESS"))
                .count();
        long activeOrders = orders.stream()
                .filter(o -> o.getStatus().name().equals("PENDING") || o.getStatus().name().equals("PROCESSING") || o.getStatus().name().equals("SHIPPED"))
                .count();

        model.addAttribute("stats", Map.of(
                "activeConsultations", activeConsultations,
                "activeOrders", activeOrders,
                "totalOrders", orders.size(),
                "unusedPrescriptions", unusedPrescriptions,
                "totalPrescriptions", totalPrescriptions
        ));

        model.addAttribute("recentConsultations", consultations.stream().limit(3).toList());
        model.addAttribute("recentOrders", orders.stream().limit(3).toList());
        model.addAttribute("recentPrescriptions", recentPrescriptions);
        setActiveSection(model, "dashboard");

        return "customer/dashboard";
    }

    @GetMapping("/medicines")
    public String showMedicines(
            HttpSession session,
            Model model,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String sort
    ) {
        Optional<Customer> optionalCustomer = findAuthenticatedCustomer(session);
        if (optionalCustomer.isEmpty()) {
            return "redirect:/auth/login";
        }

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
        setActiveSection(model, "medicines");

        return "customer/medicines";
    }

    @GetMapping("/medicines/{id}")
    public String showMedicineDetail(
            @PathVariable Long id,
            HttpSession session,
            Model model
    ) {
        Optional<Customer> optionalCustomer = findAuthenticatedCustomer(session);
        if (optionalCustomer.isEmpty()) {
            return "redirect:/auth/login";
        }

        Optional<InventoryItem> optionalItem = inventoryQueryApi.getInventoryItem(id);
        if (optionalItem.isEmpty()) {
            return "redirect:/customer/medicines";
        }

        model.addAttribute("medicine", optionalItem.get());
        setActiveSection(model, "medicines");

        return "customer/medicine-detail";
    }

    @PostMapping("/cart/add")
    public String addToCart(
            HttpSession session,
            @RequestParam Long medicineId,
            @RequestParam(defaultValue = "1") int quantity
    ) {
        Optional<Customer> optionalCustomer = findAuthenticatedCustomer(session);
        if (optionalCustomer.isEmpty()) {
            return "redirect:/auth/login";
        }

        Customer customer = optionalCustomer.get();
        Optional<InventoryItem> optionalItem = inventoryQueryApi.getInventoryItem(medicineId);
        if (optionalItem.isEmpty()) {
            return "redirect:/customer/medicines";
        }

        InventoryItem inventoryItem = optionalItem.get();

        Optional<CartItem> optionalCartItem = cartItemRepository.findByCustomerUserIdAndInventoryItemInventoryItemId(customer.getUserId(), inventoryItem.getInventoryItemId());
        CartItem cartItem;
        if (optionalCartItem.isPresent()) {
            cartItem = optionalCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            cartItem = new CartItem(customer, inventoryItem, quantity);
        }
        cartItemRepository.save(cartItem);

        return "redirect:/customer/cart";
    }

    @GetMapping("/cart")
    public String showCart(HttpSession session, Model model) {
        Optional<Customer> optionalCustomer = findAuthenticatedCustomer(session);
        if (optionalCustomer.isEmpty()) {
            return "redirect:/auth/login";
        }

        Customer customer = optionalCustomer.get();
        List<CartItem> cartItems = cartItemRepository.findByCustomerUserId(customer.getUserId());
        double subtotal = cartItems.stream().mapToDouble(CartItem::getSubtotal).sum();
        boolean hasPrescription = cartItems.stream()
                .anyMatch(item -> item.getInventoryItem().getMedicine().isRequiresPrescription());

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("adminFee", 2500);
        model.addAttribute("hasItemsRequiringPrescription", hasPrescription);
        setActiveSection(model, "cart");

        return "customer/cart";
    }

    @PostMapping("/cart/update/{id}")
    public String updateCartItem(
            HttpSession session,
            @PathVariable Long id,
            @RequestParam String action
    ) {
        Optional<Customer> optionalCustomer = findAuthenticatedCustomer(session);
        if (optionalCustomer.isEmpty()) {
            return "redirect:/auth/login";
        }

        Optional<CartItem> optionalCartItem = cartItemRepository.findById(id);
        if (optionalCartItem.isPresent()) {
            CartItem cartItem = optionalCartItem.get();
            if (!customerOwnsItem(optionalCustomer.get(), cartItem)) {
                return "redirect:/customer/cart";
            }

            if ("increase".equals(action)) {
                cartItem.setQuantity(cartItem.getQuantity() + 1);
            } else if ("decrease".equals(action) && cartItem.getQuantity() > 1) {
                cartItem.setQuantity(cartItem.getQuantity() - 1);
            }
            cartItemRepository.save(cartItem);
        }

        return "redirect:/customer/cart";
    }

    @PostMapping("/cart/remove/{id}")
    public String removeCartItem(HttpSession session, @PathVariable Long id) {
        Optional<Customer> optionalCustomer = findAuthenticatedCustomer(session);
        if (optionalCustomer.isEmpty()) {
            return "redirect:/auth/login";
        }

        Optional<CartItem> optionalCartItem = cartItemRepository.findById(id);
        if (optionalCartItem.isPresent()) {
            CartItem cartItem = optionalCartItem.get();
            if (customerOwnsItem(optionalCustomer.get(), cartItem)) {
                cartItemRepository.delete(cartItem);
            }
        }

        return "redirect:/customer/cart";
    }

    @GetMapping("/checkout")
    public String showCheckout(HttpSession session, Model model) {
        Optional<Customer> optionalCustomer = findAuthenticatedCustomer(session);
        if (optionalCustomer.isEmpty()) {
            return "redirect:/auth/login";
        }

        Customer customer = optionalCustomer.get();
        List<CartItem> cartItems = cartItemRepository.findByCustomerUserId(customer.getUserId());
        if (cartItems.isEmpty()) {
            return "redirect:/customer/cart";
        }

        double subtotal = cartItems.stream().mapToDouble(CartItem::getSubtotal).sum();
        model.addAttribute("checkoutForm", new CheckoutForm());
        model.addAttribute("orderItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shippingFee", 10000);
        model.addAttribute("adminFee", 2500);
        model.addAttribute("totalAmount", subtotal + 10000 + 2500);
        setActiveSection(model, "cart");

        return "customer/checkout";
    }

    @GetMapping("/prescription/{id}/checkout")
    public String showPrescriptionCheckout(HttpSession session, Model model, @PathVariable String id, RedirectAttributes redirectAttributes) {
        Optional<Customer> optionalCustomer = findAuthenticatedCustomer(session);
        if (optionalCustomer.isEmpty()) {
            return "redirect:/auth/login";
        }

        Optional<com.telemedclinic.prescription.model.Prescription> optPrescription = prescriptionRepository.findById(id);
        if (optPrescription.isEmpty() || !optPrescription.get().getConsultation().getCustomer().getUserId().equals(optionalCustomer.get().getUserId())) {
            return "redirect:/customer/consultations";
        }

        com.telemedclinic.prescription.model.Prescription prescription = optPrescription.get();

        // Pastikan resep belum ditebus sebelumnya
        if (prescription.getIsUsed() != null && prescription.getIsUsed()) {
            redirectAttributes.addFlashAttribute("error", "Resep ini sudah pernah ditebus.");
            return "redirect:/customer/consultations";
        }

        // Konversi Item Resep menjadi format yang bisa dibaca oleh UI checkout.html (menggunakan class CartItem sebagai DTO sementara untuk tampilan)
        List<CartItem> pseudoCartItems = new java.util.ArrayList<>();
        double subtotal = 0;

        for (com.telemedclinic.prescription.model.PrescriptionItem pItem : prescription.getItems()) {
            // Cari harga obat dari Inventory
            List<InventoryItem> inventoryItems = inventoryQueryApi.searchAvailableMedicines(pItem.getMedicine().getName());
            if (!inventoryItems.isEmpty()) {
                InventoryItem invItem = inventoryItems.get(0); // Ambil stok pertama yang cocok

                CartItem pseudoItem = new CartItem(optionalCustomer.get(), invItem, pItem.getQuantity());
                pseudoCartItems.add(pseudoItem);

                subtotal += (invItem.getPrice() * pItem.getQuantity());
            }
        }

        model.addAttribute("checkoutForm", new CheckoutForm());
        model.addAttribute("orderItems", pseudoCartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shippingFee", 10000);
        model.addAttribute("adminFee", 2500);
        model.addAttribute("totalAmount", subtotal + 10000 + 2500);
        model.addAttribute("isPrescriptionCheckout", true);
        model.addAttribute("prescriptionId", prescription.getPrescriptionId());
        setActiveSection(model, "consultations");

        return "customer/checkout";
    }

    @PostMapping("/prescription/{id}/checkout")
    @Transactional
    public String placePrescriptionOrder(HttpSession session, @PathVariable String id, @ModelAttribute CheckoutForm checkoutForm) {
        Optional<Customer> optionalCustomer = findAuthenticatedCustomer(session);
        if (optionalCustomer.isEmpty()) return "redirect:/auth/login";
        
        Customer customer = optionalCustomer.get();
        Optional<com.telemedclinic.prescription.model.Prescription> optPrescription = prescriptionRepository.findById(id);
        if (optPrescription.isEmpty() || !optPrescription.get().getConsultation().getCustomer().getUserId().equals(customer.getUserId())) {
            return "redirect:/customer/consultations";
        }
        
        com.telemedclinic.prescription.model.Prescription prescription = optPrescription.get();
        if (prescription.getIsUsed() != null && prescription.getIsUsed()) {
            return "redirect:/customer/consultations";
        }
        
        Order order = new Order(customer);
        order.setRecipientName(checkoutForm.getRecipientName());
        order.setRecipientPhone(checkoutForm.getRecipientPhone());
        order.setDeliveryAddress(checkoutForm.getDeliveryAddress());
        order.setDeliveryMethod(checkoutForm.getDeliveryMethod() != null ? checkoutForm.getDeliveryMethod() : DeliveryMethod.REGULAR);
        order.setPaymentMethod(checkoutForm.getPaymentMethod() != null ? checkoutForm.getPaymentMethod() : PaymentMethod.COD);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setPrescriptionId(prescription.getPrescriptionId());
        
        Long pharmacyId = null;
        double subtotal = 0;
        for (com.telemedclinic.prescription.model.PrescriptionItem pItem : prescription.getItems()) {
            List<InventoryItem> inventoryItems = inventoryQueryApi.searchAvailableMedicines(pItem.getMedicine().getName());
            if (!inventoryItems.isEmpty()) {
                InventoryItem invItem = inventoryItems.get(0);
                if (pharmacyId == null && invItem.getPharmacy() != null) {
                    pharmacyId = invItem.getPharmacy().getPharmacyId();
                }
                order.addItem(new OrderItem(
                    invItem.getMedicine().getName(),
                    invItem.getPharmacy().getName(),
                    pItem.getQuantity(),
                    invItem.getPrice(),
                    invItem.getMedicine().isRequiresPrescription()
                ));
                subtotal += (invItem.getPrice() * pItem.getQuantity());
            }
        }
        
        order.setPharmacyId(pharmacyId);
        order.setShippingFee(10000);
        order.setAdminFee(2500);
        order.setTotalAmount(subtotal + order.getShippingFee() + order.getAdminFee());
        
        orderRepository.save(order);
        
        // Tandai resep sudah digunakan
        prescription.setIsUsed(true);
        prescriptionRepository.save(prescription);
        
        if (order.getPaymentMethod() != PaymentMethod.COD) {
            String snapToken = midtransService.createTransaction(order);
            if (snapToken != null) {
                order.setSnapToken(snapToken);
                orderRepository.save(order);
                return "redirect:/customer/payment/" + order.getOrderId();
            }
        } else {
            order.setStatus(OrderStatus.PROCESSING);
            orderRepository.save(order);
        }
        
        return "redirect:/customer/orders";
    }

    @PostMapping("/checkout")
    @Transactional
    public String placeOrder(HttpSession session, @ModelAttribute CheckoutForm checkoutForm) {
        Optional<Customer> optionalCustomer = findAuthenticatedCustomer(session);
        if (optionalCustomer.isEmpty()) {
            return "redirect:/auth/login";
        }

        Customer customer = optionalCustomer.get();
        List<CartItem> cartItems = cartItemRepository.findByCustomerUserId(customer.getUserId());
        if (cartItems.isEmpty()) {
            return "redirect:/customer/cart";
        }

        Order order = new Order(customer);
        order.setRecipientName(checkoutForm.getRecipientName());
        order.setRecipientPhone(checkoutForm.getRecipientPhone());
        order.setDeliveryAddress(checkoutForm.getDeliveryAddress());
        order.setDeliveryMethod(checkoutForm.getDeliveryMethod() != null ? checkoutForm.getDeliveryMethod() : DeliveryMethod.REGULAR);
        order.setPaymentMethod(checkoutForm.getPaymentMethod() != null ? checkoutForm.getPaymentMethod() : PaymentMethod.COD);
        order.setPaymentStatus(PaymentStatus.PENDING);

        Long pharmacyId = null;
        for (CartItem cartItem : cartItems) {
            InventoryItem inventoryItem = cartItem.getInventoryItem();
            if (pharmacyId == null && inventoryItem.getPharmacy() != null) {
                pharmacyId = inventoryItem.getPharmacy().getPharmacyId();
            }
            order.addItem(new OrderItem(
                    inventoryItem.getMedicine().getName(),
                    inventoryItem.getPharmacy().getName(),
                    cartItem.getQuantity(),
                    inventoryItem.getPrice(),
                    inventoryItem.getMedicine().isRequiresPrescription()
            ));
        }
        order.setPharmacyId(pharmacyId);
        order.setShippingFee(10000);
        order.setAdminFee(2500);
        order.setTotalAmount(order.getSubtotal() + order.getShippingFee() + order.getAdminFee());
        
        orderRepository.save(order);

        if (order.getPaymentMethod() != PaymentMethod.COD) {
            String snapToken = midtransService.createTransaction(order);
            if (snapToken != null) {
                order.setSnapToken(snapToken);
                orderRepository.save(order);
            }
            cartItemRepository.deleteByCustomerUserId(customer.getUserId());
            
            // Redirect ke halaman pembayaran khusus jika token ada
            if (order.getSnapToken() != null) {
                return "redirect:/customer/payment/" + order.getOrderId();
            } else {
                return "redirect:/customer/orders";
            }
        } else {
            order.setStatus(OrderStatus.PROCESSING);
            orderRepository.save(order);
        }
        
        cartItemRepository.deleteByCustomerUserId(customer.getUserId());

        return "redirect:/customer/orders";
    }

    @GetMapping("/payment/{orderNumber}")
    public String showPaymentPage(HttpSession session, Model model, @PathVariable String orderNumber) {
        Optional<Customer> optionalCustomer = findAuthenticatedCustomer(session);
        if (optionalCustomer.isEmpty()) {
            return "redirect:/auth/login";
        }

        Optional<Order> optionalOrder = orderRepository.findByOrderId(orderNumber);
        if (optionalOrder.isEmpty() || !optionalOrder.get().getCustomer().getUserId().equals(optionalCustomer.get().getUserId())) {
            return "redirect:/customer/orders";
        }

        Order order = optionalOrder.get();
        if (order.getSnapToken() == null) {
            return "redirect:/customer/orders";
        }

        model.addAttribute("order", order);
        model.addAttribute("snapToken", order.getSnapToken());
        model.addAttribute("midtransClientKey", midtransClientKey);
        setActiveSection(model, "orders");

        return "customer/payment-midtrans";
    }

    @GetMapping("/orders")
    public String showOrders(HttpSession session, Model model, @RequestParam(required = false) String status) {
        Optional<Customer> optionalCustomer = findAuthenticatedCustomer(session);
        if (optionalCustomer.isEmpty()) {
            return "redirect:/auth/login";
        }

        Customer customer = optionalCustomer.get();
        List<Order> orders;
        if (status != null && !status.isBlank()) {
            try {
                orders = orderRepository.findByCustomerUserIdAndStatusOrderByCreatedAtDesc(customer.getUserId(), OrderStatus.valueOf(status));
            } catch (IllegalArgumentException ex) {
                orders = orderRepository.findByCustomerUserIdOrderByCreatedAtDesc(customer.getUserId());
            }
        } else {
            orders = orderRepository.findByCustomerUserIdOrderByCreatedAtDesc(customer.getUserId());
        }

        long pendingCount = orderRepository.findByCustomerUserIdAndStatusOrderByCreatedAtDesc(customer.getUserId(), OrderStatus.PENDING).size();
        long processingCount = orderRepository.findByCustomerUserIdAndStatusOrderByCreatedAtDesc(customer.getUserId(), OrderStatus.PROCESSING).size();
        long shippedCount = orderRepository.findByCustomerUserIdAndStatusOrderByCreatedAtDesc(customer.getUserId(), OrderStatus.SHIPPED).size();
  
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("processingCount", processingCount);
        model.addAttribute("shippedCount", shippedCount);

        model.addAttribute("orders", orders);
        model.addAttribute("status", status);
        model.addAttribute("midtransClientKey", midtransClientKey);
        setActiveSection(model, "orders");

        return "customer/orders";
    }

    @GetMapping("/orders/{id}")
    public String showOrderDetail(HttpSession session, Model model, @PathVariable Long id) {
        Optional<Customer> optionalCustomer = findAuthenticatedCustomer(session);
        if (optionalCustomer.isEmpty()) {
            return "redirect:/auth/login";
        }

        Optional<Order> optionalOrder = orderRepository.findById(id);
        if (optionalOrder.isEmpty() || !optionalOrder.get().getCustomer().getUserId().equals(optionalCustomer.get().getUserId())) {
            return "redirect:/customer/orders";
        }

        model.addAttribute("order", optionalOrder.get());
        model.addAttribute("midtransClientKey", midtransClientKey);
        setActiveSection(model, "orders");
        return "customer/order-detail";
    }

    @GetMapping("/consultations")
    public String showConsultations(HttpSession session, Model model, @RequestParam(required = false) String status) {
        Optional<Customer> optionalCustomer = findAuthenticatedCustomer(session);
        if (optionalCustomer.isEmpty()) {
            return "redirect:/auth/login";
        }

        Customer customer = optionalCustomer.get();
        // Tarik semua konsultasi milik user ini dari repository
        List<Consultation> allConsultations = consultationRepository.findByCustomerUserIdOrderByCreatedAtDesc(customer.getUserId());
        
        // Hitung masing-masing status untuk Badge
        long countWaiting = allConsultations.stream().filter(c -> c.getStatus().name().equals("WAITING")).count();
        long countInProgress = allConsultations.stream().filter(c -> c.getStatus().name().equals("IN_PROGRESS")).count();
        long countCompleted = allConsultations.stream().filter(c -> c.getStatus().name().equals("COMPLETED")).count();
        long countAll = allConsultations.size();

        // Lakukan filtering data berdasarkan parameter status
        List<Consultation> filteredConsultations;
        if (status == null || status.isBlank()) {
            filteredConsultations = allConsultations;
        } else {
            filteredConsultations = allConsultations.stream()
                .filter(c -> c.getStatus().name().equals(status))
                .collect(java.util.stream.Collectors.toList());
        }

        model.addAttribute("consultations", filteredConsultations);
        model.addAttribute("status", status);
        model.addAttribute("countAll", countAll);
        model.addAttribute("countWaiting", countWaiting);
        model.addAttribute("countInProgress", countInProgress);
        model.addAttribute("countCompleted", countCompleted);
        
        setActiveSection(model, "consultations");
        return "customer/consultations";
    }

    @GetMapping("/consultations/new")
    public String showConsultationForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Optional<Customer> optionalCustomer = findAuthenticatedCustomer(session);
        if (optionalCustomer.isEmpty()) {
            return "redirect:/auth/login";
        }

        Customer customer = optionalCustomer.get();
        boolean hasActiveConsultation = consultationRepository.existsByCustomer_UserIdAndStatusIn(
            customer.getUserId(),
            java.util.Arrays.asList(com.telemedclinic.consultation.model.ConsultationStatus.WAITING, com.telemedclinic.consultation.model.ConsultationStatus.IN_PROGRESS)
        );
        
        if (hasActiveConsultation) {
            redirectAttributes.addFlashAttribute("error", "Selesaikan atau batalkan sesi konsultasi Anda yang sedang berjalan sebelum membuat jadwal baru.");
            return "redirect:/customer/consultations";
        }

        // Gunakan lambda aman untuk menghindari error pemetaan pewarisan class User
        List<Doctor> availableDoctors = doctorRepository.findAll().stream()
                .filter(Doctor::isApprovedPartner)
                .filter(Doctor::isActive)
                .filter(Doctor::isPracticeActiveNow)
                .toList();

        model.addAttribute("availableDoctors", availableDoctors);
        model.addAttribute("consultationForm", new ConsultationForm());
        setActiveSection(model, "consultations");
        
        return "customer/consultation-new";
    }

    @PostMapping("/consultations/new")
    public String submitConsultationForm(HttpSession session, Model model, @ModelAttribute ConsultationForm consultationForm, RedirectAttributes redirectAttributes) {
        Optional<Customer> optionalCustomer = findAuthenticatedCustomer(session);
        if (optionalCustomer.isEmpty()) {
            return "redirect:/auth/login";
        }

        Customer customer = optionalCustomer.get();
        boolean hasActiveConsultation = consultationRepository.existsByCustomer_UserIdAndStatusIn(
            customer.getUserId(),
            java.util.Arrays.asList(com.telemedclinic.consultation.model.ConsultationStatus.WAITING, com.telemedclinic.consultation.model.ConsultationStatus.IN_PROGRESS)
        );
        
        if (hasActiveConsultation) {
            redirectAttributes.addFlashAttribute("error", "Selesaikan atau batalkan sesi konsultasi Anda yang sedang berjalan sebelum membuat jadwal baru.");
            return "redirect:/customer/consultations";
        }

        if (consultationForm.getDoctorId() == null || consultationForm.getComplaint() == null || consultationForm.getComplaint().isBlank()) {
            model.addAttribute("error", "Pilih dokter dan jelaskan keluhan Anda.");
            model.addAttribute("availableDoctors", doctorRepository.findAll().stream()
                    .filter(Doctor::isApprovedPartner)
                    .filter(Doctor::isActive)
                    .filter(Doctor::isPracticeActiveNow).toList());
            model.addAttribute("consultationForm", consultationForm);
            setActiveSection(model, "consultations");
            return "customer/consultation-new";
        }

        Optional<Doctor> optionalDoctor = doctorRepository.findById(consultationForm.getDoctorId());
        if (optionalDoctor.isEmpty()) {
            model.addAttribute("error", "Dokter tidak ditemukan.");
            model.addAttribute("availableDoctors", doctorRepository.findAll().stream()
                    .filter(Doctor::isApprovedPartner)
                    .filter(Doctor::isActive)
                    .filter(Doctor::isPracticeActiveNow).toList());
            model.addAttribute("consultationForm", consultationForm);
            setActiveSection(model, "consultations");
            return "customer/consultation-new";
        }

        Consultation consultation = new Consultation(
                optionalCustomer.get(),
                optionalDoctor.get(),
                consultationForm.getComplaint(),
                consultationForm.getAdditionalInfo()
        );
        consultationRepository.save(consultation);

        String snapToken = midtransService.createConsultationTransaction(consultation);
        if (snapToken != null) {
            consultation.setSnapToken(snapToken);
            consultationRepository.save(consultation);
            return "redirect:/customer/consultations/payment/" + consultation.getId();
        }

        return "redirect:/customer/consultations";
    }

    @GetMapping("/consultations/payment/{id}")
    public String showConsultationPaymentPage(HttpSession session, Model model, @PathVariable Long id) {
        Optional<Customer> optionalCustomer = findAuthenticatedCustomer(session);
        if (optionalCustomer.isEmpty()) return "redirect:/auth/login";

        Optional<Consultation> optionalConsultation = consultationRepository.findById(id);
        if (optionalConsultation.isEmpty() || !optionalConsultation.get().getCustomer().getUserId().equals(optionalCustomer.get().getUserId())) {
            return "redirect:/customer/consultations";
        }

        Consultation consultation = optionalConsultation.get();
        if (consultation.getSnapToken() == null || consultation.getStatus() != ConsultationStatus.PENDING) {
            return "redirect:/customer/consultations";
        }

        model.addAttribute("consultation", consultation);
        model.addAttribute("snapToken", consultation.getSnapToken());
        model.addAttribute("midtransClientKey", midtransClientKey);
        setActiveSection(model, "consultations");

        return "customer/payment-midtrans-consultation";
    }

    @GetMapping("/consultations/{id}")
    public String showConsultationDetail(HttpSession session, Model model, @PathVariable Long id) {
        Optional<Customer> optionalCustomer = findAuthenticatedCustomer(session);
        if (optionalCustomer.isEmpty()) {
            return "redirect:/auth/login";
        }

        Optional<Consultation> optionalConsultation = consultationRepository.findById(id);
        if (optionalConsultation.isEmpty() || !optionalConsultation.get().getCustomer().getUserId().equals(optionalCustomer.get().getUserId())) {
            return "redirect:/customer/consultations";
        }

        Consultation consultation = optionalConsultation.get();
        model.addAttribute("consultation", consultation);
        java.util.List<com.telemedclinic.consultation.model.ChatMessage> messages = chatMessageRepository.findByConsultation_IdOrderByCreatedAtAsc(consultation.getId());
        model.addAttribute("messages", messages);
        model.addAttribute("currentUserEmail", session.getAttribute("currentUserEmail"));
        
        Optional<com.telemedclinic.prescription.model.Prescription> prescription = prescriptionRepository.findByConsultationId(consultation.getId());
        prescription.ifPresent(value -> model.addAttribute("prescription", value));
        
        setActiveSection(model, "consultations");
        return "customer/consultation-chat";
    }

    @PostMapping("/consultations/{id}/message")
    public String sendConsultationMessage(HttpSession session, @PathVariable Long id, @RequestParam String message) {
        Optional<Customer> optionalCustomer = findAuthenticatedCustomer(session);
        if (optionalCustomer.isEmpty()) {
            return "redirect:/auth/login";
        }

        Optional<Consultation> optionalConsultation = consultationRepository.findById(id);
        if (optionalConsultation.isEmpty() || !optionalConsultation.get().getCustomer().getUserId().equals(optionalCustomer.get().getUserId())) {
            return "redirect:/customer/consultations";
        }

        return "redirect:/customer/consultations/" + id;
    }

    @PostMapping("/consultations/{id}/diagnosis")
    public String submitConsultationDiagnosis(HttpSession session, @PathVariable Long id, @RequestParam String diagnosis) {
        Optional<Customer> optionalCustomer = findAuthenticatedCustomer(session);
        if (optionalCustomer.isEmpty()) {
            return "redirect:/auth/login";
        }

        Optional<Consultation> optionalConsultation = consultationRepository.findById(id);
        if (optionalConsultation.isEmpty() || !optionalConsultation.get().getCustomer().getUserId().equals(optionalCustomer.get().getUserId())) {
            return "redirect:/customer/consultations";
        }

        Consultation consultation = optionalConsultation.get();
        consultation.setStatus(ConsultationStatus.COMPLETED);
        consultationRepository.save(consultation);

        return "redirect:/customer/consultations/" + id;
    }

    @PostMapping("/consultations/{id}/check-status")
    public String checkMidtransStatusManual(@PathVariable Long id, HttpSession session, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Optional<com.telemedclinic.consultation.model.Consultation> opt = consultationRepository.findById(id);
        if (opt.isPresent()) {
            com.telemedclinic.consultation.model.Consultation consultation = opt.get();
            try {
                // Panggil API Status Midtrans Sandbox
                String orderId = "CONS-" + consultation.getId();
                String authString = midtransServerKey + ":";
                String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes());
                
                URL url = new URL("https://api.sandbox.midtrans.com/v2/" + orderId + "/status");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
                conn.setRequestProperty("Content-Type", "application/json");
                
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) { response.append(inputLine); }
                    in.close();
                    
                    String resBody = response.toString();
                    if (resBody.contains("\"transaction_status\":\"settlement\"") || resBody.contains("\"transaction_status\":\"capture\"")) {
                        consultation.setStatus(com.telemedclinic.consultation.model.ConsultationStatus.WAITING);
                        consultationRepository.save(consultation);
                        redirectAttributes.addFlashAttribute("success", "Pembayaran terverifikasi! Menunggu konfirmasi dokter.");
                    } else if (resBody.contains("\"transaction_status\":\"expire\"") || resBody.contains("\"transaction_status\":\"cancel\"")) {
                        consultation.setStatus(com.telemedclinic.consultation.model.ConsultationStatus.CANCELLED);
                        consultationRepository.save(consultation);
                        redirectAttributes.addFlashAttribute("error", "Transaksi telah kedaluwarsa atau dibatalkan di Midtrans.");
                    } else {
                        redirectAttributes.addFlashAttribute("info", "Midtrans menyatakan pembayaran Anda masih belum masuk.");
                    }
                } else {
                    redirectAttributes.addFlashAttribute("error", "Gagal menghubungi server pembayaran.");
                }
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Terjadi kesalahan sistem saat mengecek pembayaran.");
            }
        }
        return "redirect:/customer/consultations";
    }

    @PostMapping("/consultations/{id}/delete")
    public String deleteConsultation(@PathVariable Long id, HttpSession session) {
        Optional<com.telemedclinic.consultation.model.Consultation> opt = consultationRepository.findById(id);
        if (opt.isPresent() && (opt.get().getStatus() == com.telemedclinic.consultation.model.ConsultationStatus.CANCELLED || opt.get().getStatus() == com.telemedclinic.consultation.model.ConsultationStatus.PENDING)) {
            consultationRepository.delete(opt.get());
        }
        return "redirect:/customer/consultations";
    }

    private Optional<Customer> findAuthenticatedCustomer(HttpSession session) {
        String roleValue = session.getAttribute("currentUserRole") != null
                ? session.getAttribute("currentUserRole").toString()
                : null;

        if (!Role.ROLE_CUSTOMER.name().equals(roleValue)) {
            return Optional.empty();
        }

        String customerEmail = (String) session.getAttribute("currentUserEmail");
        if (customerEmail == null) {
            return Optional.empty();
        }

        Optional<User> optionalUser = userRepository.findByEmail(customerEmail);
        if (optionalUser.isEmpty() || !(optionalUser.get() instanceof Customer customer)) {
            return Optional.empty();
        }

        return Optional.of(customer);
    }

    private boolean customerOwnsItem(Customer customer, CartItem cartItem) {
        return cartItem.getCustomer() != null && cartItem.getCustomer().getUserId().equals(customer.getUserId());
    }

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        Optional<Customer> optionalCustomer = findAuthenticatedCustomer(session);
        if (optionalCustomer.isEmpty()) {
            return "redirect:/auth/login";
        }

        Customer customer = optionalCustomer.get();
        model.addAttribute("profileName", customer.getName());
        model.addAttribute("profileEmail", customer.getEmail());
        model.addAttribute("profilePhone", customer.getPhoneNumber());
        model.addAttribute("profileAddress", customer.getAddress());
        model.addAttribute("profileHeight", customer.getHeight());
        model.addAttribute("profileWeight", customer.getWeight());
        model.addAttribute("profileBirthDate", customer.getBirthDate());
        model.addAttribute("profileGender", customer.getGender());
        model.addAttribute("profileMemberSince", customer.getCreatedAt());
        model.addAttribute("profilePhoto", customer.getProfilePhotoPath() != null ? customer.getProfilePhotoPath() : "");

        setActiveSection(model, "profile");
        return "customer/customer-profile";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(
            HttpSession session,
            @RequestParam String name,
            @RequestParam String phoneNumber,
            @RequestParam String address,
            @RequestParam Gender gender,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate,
            @RequestParam Double height,
            @RequestParam Double weight,
            @RequestParam(required = false) MultipartFile profilePhoto,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Customer> optionalCustomer = findAuthenticatedCustomer(session);
        if (optionalCustomer.isEmpty()) {
            return "redirect:/auth/login";
        }

        Customer customer = optionalCustomer.get();
        customer.updateProfile(name, phoneNumber, address, gender, birthDate, height, weight);

        if (profilePhoto != null && !profilePhoto.isEmpty()) {
            try {
                String fileName = StringUtils.cleanPath(profilePhoto.getOriginalFilename());
                String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
                String uploadDir = "uploads/profile-photos";
                Path uploadPath = Paths.get(uploadDir);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path filePath = uploadPath.resolve(uniqueFileName);
                Files.copy(profilePhoto.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                
                customer.setProfilePhotoPath("/uploads/profile-photos/" + uniqueFileName);
            } catch (Exception ex) {
                redirectAttributes.addFlashAttribute("error", "Gagal mengunggah foto profil: " + ex.getMessage());
                return "redirect:/customer/profile";
            }
        }

        userRepository.save(customer);
        redirectAttributes.addFlashAttribute("success", "Profil berhasil diperbarui!");
        return "redirect:/customer/profile";
    }

  @GetMapping("/prescription/download/{id}")
  public void downloadPrescriptionPdf(@PathVariable Long id, HttpServletResponse response) {
      try {
          // Cari resep berdasarkan ID
          Optional<com.telemedclinic.prescription.model.Prescription> optPrescription = prescriptionRepository.findById(String.valueOf(id));
          if (optPrescription.isEmpty()) return;
          
          com.telemedclinic.prescription.model.Prescription prescription = optPrescription.get();
          com.telemedclinic.consultation.model.Consultation consultation = prescription.getConsultation();

          // Set response headers agar browser mendownload file
          response.setContentType("application/pdf");
          String headerKey = "Content-Disposition";
          String headerValue = "attachment; filename=Resep_KlinikKu_" + consultation.getId() + ".pdf";
          response.setHeader(headerKey, headerValue);

          // Inisialisasi Dokumen PDF
          Document document = new Document();
          PdfWriter.getInstance(document, response.getOutputStream());
          document.open();

          // Styling Font
          com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
          com.lowagie.text.Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
          com.lowagie.text.Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
          com.lowagie.text.Font boldTextFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

          // Tulis Header Klinik
          Paragraph title = new Paragraph("KLINIKKU - RESEP DIGITAL", titleFont);
          title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
          document.add(title);
          
          Paragraph subTitle = new Paragraph("Layanan Konsultasi Telemedicine Terpercaya", subTitleFont);
          subTitle.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
          document.add(subTitle);
          
          document.add(new Paragraph(" ")); // Spasi kosong
          document.add(new Paragraph("---------------------------------------------------------------------------------------------------"));

          // Tulis Info Pasien & Dokter
          document.add(new Paragraph("ID Konsultasi : CONS-" + consultation.getId(), boldTextFont));
          document.add(new Paragraph("Dokter        : " + consultation.getDoctorName(), textFont));
          document.add(new Paragraph("Spesialisasi  : " + consultation.getDoctorSpecialization(), textFont));
          document.add(new Paragraph("Keluhan       : " + consultation.getComplaint(), textFont));
          
          document.add(new Paragraph("---------------------------------------------------------------------------------------------------"));
          document.add(new Paragraph(" "));

          // Tulis Daftar Obat
          Paragraph medicineTitle = new Paragraph("DAFTAR OBAT YANG DIRESEPKAN:", boldTextFont);
          document.add(medicineTitle);
          document.add(new Paragraph(" "));

          for (com.telemedclinic.prescription.model.PrescriptionItem item : prescription.getItems()) {
              String medName = item.getMedicine().getName();
              String instructions = item.getInstructions();
              Integer qty = item.getQuantity();
              
              Paragraph medPara = new Paragraph("- " + medName + " (" + qty + " pcs)", boldTextFont);
              Paragraph instPara = new Paragraph("  Aturan Pakai: " + instructions, textFont);
              
              document.add(medPara);
              document.add(instPara);
              document.add(new Paragraph(" ")); // Spasi antar obat
          }

          document.add(new Paragraph("---------------------------------------------------------------------------------------------------"));
          Paragraph footer = new Paragraph("Resep ini di-generate secara otomatis oleh sistem KlinikKu dan sah untuk digunakan di apotek luar.", com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_OBLIQUE, 9));
          footer.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
          document.add(footer);

          // Tutup dokumen
          document.close();
      } catch (Exception e) {
          e.printStackTrace();
      }
  }
}
