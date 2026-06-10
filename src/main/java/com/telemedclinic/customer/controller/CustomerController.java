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
import java.time.LocalDate;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
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
import com.telemedclinic.inventory.entity.InventoryItem;
import com.telemedclinic.inventory.repository.InventoryItemRepository;
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
    private final InventoryItemRepository inventoryItemRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final ConsultationRepository consultationRepository;

    @Autowired
    private MidtransService midtransService;

    @Value("${midtrans.client.key:}")
    private String midtransClientKey;

    public CustomerController(
            UserRepository userRepository,
            DoctorRepository doctorRepository,
            PrescriptionRepository prescriptionRepository,
            InventoryItemRepository inventoryItemRepository,
            CartItemRepository cartItemRepository,
            OrderRepository orderRepository,
            ConsultationRepository consultationRepository
    ) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.inventoryItemRepository = inventoryItemRepository;
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
            medicines = inventoryItemRepository.findByMedicine_NameContainingIgnoreCase(search);
        } else {
            medicines = inventoryItemRepository.findAll();
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

        Optional<InventoryItem> optionalItem = inventoryItemRepository.findById(id);
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
        Optional<InventoryItem> optionalItem = inventoryItemRepository.findById(medicineId);
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
        List<Consultation> consultations;
        if (status != null && !status.isBlank()) {
            try {
                consultations = consultationRepository.findByCustomerUserIdAndStatusOrderByCreatedAtDesc(
                        customer.getUserId(), ConsultationStatus.valueOf(status));
            } catch (IllegalArgumentException ex) {
                consultations = consultationRepository.findByCustomerUserIdOrderByCreatedAtDesc(customer.getUserId());
            }
        } else {
            consultations = consultationRepository.findByCustomerUserIdOrderByCreatedAtDesc(customer.getUserId());
        }

        model.addAttribute("consultations", consultations);
        model.addAttribute("status", status);
        setActiveSection(model, "consultations");
        return "customer/consultations";
    }

    @GetMapping("/consultations/new")
    public String showConsultationForm(HttpSession session, Model model) {
        Optional<Customer> optionalCustomer = findAuthenticatedCustomer(session);
        if (optionalCustomer.isEmpty()) {
            return "redirect:/auth/login";
        }

        // Gunakan lambda aman untuk menghindari error pemetaan pewarisan class User
        List<Doctor> availableDoctors = doctorRepository.findAll().stream()
                .filter(Doctor::isApprovedPartner)
                .filter(doctor -> doctor.isActive()) 
                .toList();

        model.addAttribute("availableDoctors", availableDoctors);
        model.addAttribute("consultationForm", new ConsultationForm());
        setActiveSection(model, "consultations");
        
        return "customer/consultation-new";
    }

    @PostMapping("/consultations/new")
    public String submitConsultationForm(HttpSession session, Model model, @ModelAttribute ConsultationForm consultationForm) {
        Optional<Customer> optionalCustomer = findAuthenticatedCustomer(session);
        if (optionalCustomer.isEmpty()) {
            return "redirect:/auth/login";
        }

        if (consultationForm.getDoctorId() == null || consultationForm.getComplaint() == null || consultationForm.getComplaint().isBlank()) {
            model.addAttribute("error", "Pilih dokter dan jelaskan keluhan Anda.");
            model.addAttribute("availableDoctors", doctorRepository.findAll().stream()
                    .filter(Doctor::isApprovedPartner)
                    .filter(doctor -> doctor.isActive()).toList());
            model.addAttribute("consultationForm", consultationForm);
            setActiveSection(model, "consultations");
            return "customer/consultation-new";
        }

        Optional<Doctor> optionalDoctor = doctorRepository.findById(consultationForm.getDoctorId());
        if (optionalDoctor.isEmpty()) {
            model.addAttribute("error", "Dokter tidak ditemukan.");
            model.addAttribute("availableDoctors", doctorRepository.findAll().stream()
                    .filter(Doctor::isApprovedPartner)
                    .filter(doctor -> doctor.isActive()).toList());
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

        return "redirect:/customer/consultations";
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

        model.addAttribute("consultation", optionalConsultation.get());
        model.addAttribute("messages", List.of());
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

    @PostMapping("/consultations/delete/{id}")
    public String deleteConsultation(HttpSession session, @PathVariable Long id) {
        Optional<Customer> optionalCustomer = findAuthenticatedCustomer(session);
        if (optionalCustomer.isEmpty()) {
            return "redirect:/auth/login";
        }

        Optional<Consultation> optionalConsultation = consultationRepository.findById(id);
        if (optionalConsultation.isPresent()) {
            Consultation consultation = optionalConsultation.get();
            if (consultation.getCustomer().getUserId().equals(optionalCustomer.get().getUserId())
                    && consultation.getStatus().name().equals("PENDING")) {
                consultationRepository.delete(consultation);
            }
        }
        return "redirect:/customer/consultations";
    }

    @PostMapping("/consultations/edit/{id}")
    public String editConsultation(HttpSession session, @PathVariable Long id, @RequestParam String complaint, @RequestParam(required = false) String additionalInfo) {
        Optional<Customer> optionalCustomer = findAuthenticatedCustomer(session);
        if (optionalCustomer.isEmpty()) {
            return "redirect:/auth/login";
        }

        Optional<Consultation> optionalConsultation = consultationRepository.findById(id);
        if (optionalConsultation.isPresent()) {
            Consultation consultation = optionalConsultation.get();
            if (consultation.getCustomer().getUserId().equals(optionalCustomer.get().getUserId())
                    && consultation.getStatus().name().equals("PENDING")) {
                consultation.setComplaint(complaint);
                consultation.setAdditionalInfo(additionalInfo);
                consultationRepository.save(consultation);
            }
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
}
