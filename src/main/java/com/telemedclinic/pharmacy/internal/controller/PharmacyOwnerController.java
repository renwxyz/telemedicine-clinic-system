package com.telemedclinic.pharmacy.internal.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.telemedclinic.pharmacy.api.InventoryQueryApi;
import com.telemedclinic.pharmacy.api.PharmacyManagementApi;
import com.telemedclinic.pharmacy.internal.dto.CreatePharmacistRequestDTO;
import com.telemedclinic.pharmacy.internal.dto.OwnerProfileDTO;
import com.telemedclinic.pharmacy.internal.dto.PharmacySettingsDTO;
import com.telemedclinic.pharmacy.internal.dto.WithdrawalRequestDTO;
import com.telemedclinic.pharmacy.internal.entity.Pharmacy;
import com.telemedclinic.pharmacy.internal.entity.PharmacyOwner;
import com.telemedclinic.pharmacy.internal.entity.Pharmacist;
import com.telemedclinic.pharmacy.internal.entity.InventoryItem;
import com.telemedclinic.user.entity.Role;
import com.telemedclinic.user.entity.User;
import com.telemedclinic.user.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/owner/pharmacy")
@Transactional(readOnly = true)
public class PharmacyOwnerController {

    private final UserRepository userRepository;
    private final PharmacyManagementApi pharmacyManagementApi;
    private final InventoryQueryApi inventoryQueryApi;

    public PharmacyOwnerController(UserRepository userRepository, PharmacyManagementApi pharmacyManagementApi, InventoryQueryApi inventoryQueryApi) {
        this.userRepository = userRepository;
        this.pharmacyManagementApi = pharmacyManagementApi;
        this.inventoryQueryApi = inventoryQueryApi;
    }

    private Optional<PharmacyOwner> findAuthenticatedOwner(HttpSession session) {
        String roleValue = session.getAttribute("currentUserRole") != null
                ? session.getAttribute("currentUserRole").toString()
                : null;

        if (roleValue == null || !roleValue.equals(Role.ROLE_PHARMACY_OWNER.name())) {
            return Optional.empty();
        }

        String email = session.getAttribute("currentUserEmail") != null
                ? session.getAttribute("currentUserEmail").toString()
                : null;

        if (email == null) {
            return Optional.empty();
        }

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty() || !(optionalUser.get() instanceof PharmacyOwner owner)) {
            return Optional.empty();
        }

        return Optional.of(owner);
    }

    private Optional<Pharmacy> getActivePharmacy(HttpSession session, PharmacyOwner owner) {
        if (owner.getPharmacies().isEmpty()) {
            return Optional.empty();
        }
        if (owner.getPharmacies().size() == 1) {
            return Optional.of(owner.getPharmacies().get(0));
        }

        Long activePharmacyId = (Long) session.getAttribute("activePharmacyId");
        if (activePharmacyId != null) {
            return owner.getPharmacies().stream()
                    .filter(p -> p.getPharmacyId().equals(activePharmacyId))
                    .findFirst();
        }

        return Optional.empty();
    }

    @GetMapping("/select")
    public String selectPharmacy(HttpSession session, Model model) {
        Optional<PharmacyOwner> optionalOwner = findAuthenticatedOwner(session);
        if (optionalOwner.isEmpty()) {
            return "redirect:/auth/login";
        }

        PharmacyOwner owner = optionalOwner.get();
        if (owner.getPharmacies().size() <= 1) {
            return "redirect:/owner/pharmacy/dashboard";
        }

        model.addAttribute("owner", owner);
        model.addAttribute("pharmacies", owner.getPharmacies());
        return "pharmacy/owner/select-pharmacy";
    }

    @PostMapping("/select")
    public String processSelectPharmacy(HttpSession session, @RequestParam("pharmacyId") Long pharmacyId) {
        Optional<PharmacyOwner> optionalOwner = findAuthenticatedOwner(session);
        if (optionalOwner.isEmpty()) {
            return "redirect:/auth/login";
        }

        PharmacyOwner owner = optionalOwner.get();
        boolean valid = owner.getPharmacies().stream().anyMatch(p -> p.getPharmacyId().equals(pharmacyId));
        if (valid) {
            session.setAttribute("activePharmacyId", pharmacyId);
            return "redirect:/owner/pharmacy/dashboard";
        }

        return "redirect:/owner/pharmacy/select?error=invalid";
    }

    @PostMapping("/switch")
    public String switchPharmacy(HttpSession session) {
        session.removeAttribute("activePharmacyId");
        return "redirect:/owner/pharmacy/select";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Optional<PharmacyOwner> optionalOwner = findAuthenticatedOwner(session);
        if (optionalOwner.isEmpty()) {
            return "redirect:/auth/login";
        }

        PharmacyOwner owner = optionalOwner.get();
        model.addAttribute("owner", owner);
        
        Optional<Pharmacy> optPharmacy = getActivePharmacy(session, owner);
        if (optPharmacy.isEmpty() && owner.getPharmacies().size() > 1) {
            return "redirect:/owner/pharmacy/select";
        }

        if (optPharmacy.isPresent()) {
            Pharmacy pharmacy = optPharmacy.get();
            model.addAttribute("pharmacy", pharmacy);

            List<Pharmacist> pharmacists = userRepository.findAll().stream()
                .filter(u -> u instanceof Pharmacist)
                .map(u -> (Pharmacist) u)
                .filter(p -> p.getPharmacy() != null && p.getPharmacy().getPharmacyId().equals(pharmacy.getPharmacyId()))
                .toList();

            model.addAttribute("totalStaff", pharmacists.size());
            model.addAttribute("pharmacists", pharmacists);
            model.addAttribute("todayEarnings", pharmacyManagementApi.getTodayEarnings(pharmacy.getPharmacyId()));
            model.addAttribute("todayTransactions", pharmacyManagementApi.getTodayCompletedTransactionsCount(pharmacy.getPharmacyId()));

            List<InventoryItem> allItems = inventoryQueryApi.getAllAvailableMedicines().stream()
                .filter(item -> item.getPharmacy() != null && item.getPharmacy().getPharmacyId().equals(pharmacy.getPharmacyId()))
                .toList();
            model.addAttribute("totalProducts", allItems.size());
            model.addAttribute("lowStockItems", allItems.stream().filter(item -> item.getStock() <= 10).toList());
        } else {
            model.addAttribute("totalStaff", 0);
            model.addAttribute("pharmacists", java.util.List.of());
            model.addAttribute("todayEarnings", 0.0);
            model.addAttribute("todayTransactions", 0L);
            model.addAttribute("totalProducts", 0);
            model.addAttribute("lowStockItems", java.util.List.of());
        }

        return "pharmacy/owner/dashboard";
    }

    @GetMapping("/products")
    public String products(HttpSession session, Model model) {
        Optional<PharmacyOwner> optionalOwner = findAuthenticatedOwner(session);
        if (optionalOwner.isEmpty()) {
            return "redirect:/auth/login";
        }

        PharmacyOwner owner = optionalOwner.get();
        model.addAttribute("owner", owner);

        Optional<Pharmacy> optPharmacy = getActivePharmacy(session, owner);
        if (optPharmacy.isEmpty() && owner.getPharmacies().size() > 1) {
            return "redirect:/owner/pharmacy/select";
        }

        if (optPharmacy.isPresent()) {
            Pharmacy pharmacy = optPharmacy.get();
            model.addAttribute("pharmacy", pharmacy);
            
            List<InventoryItem> items = inventoryQueryApi.getAllAvailableMedicines().stream()
                .filter(item -> item.getPharmacy() != null && item.getPharmacy().getPharmacyId().equals(pharmacy.getPharmacyId()))
                .toList();
            model.addAttribute("inventoryItems", items);
        }

        return "pharmacy/owner/products";
    }

    @GetMapping("/staff")
    public String staff(HttpSession session, Model model) {
        Optional<PharmacyOwner> optionalOwner = findAuthenticatedOwner(session);
        if (optionalOwner.isEmpty()) {
            return "redirect:/auth/login";
        }

        PharmacyOwner owner = optionalOwner.get();
        model.addAttribute("owner", owner);

        Optional<Pharmacy> optPharmacy = getActivePharmacy(session, owner);
        if (optPharmacy.isEmpty() && owner.getPharmacies().size() > 1) {
            return "redirect:/owner/pharmacy/select";
        }

        if (optPharmacy.isPresent()) {
            Pharmacy pharmacy = optPharmacy.get();
            model.addAttribute("pharmacy", pharmacy);
            
            List<Pharmacist> pharmacists = userRepository.findAll().stream()
                .filter(u -> u instanceof Pharmacist)
                .map(u -> (Pharmacist) u)
                .filter(p -> p.getPharmacy() != null && p.getPharmacy().getPharmacyId().equals(pharmacy.getPharmacyId()))
                .toList();
            model.addAttribute("pharmacists", pharmacists);
        }

        return "pharmacy/owner/staff";
    }

    @GetMapping("/balance")
    public String balance(HttpSession session, Model model) {
        Optional<PharmacyOwner> optionalOwner = findAuthenticatedOwner(session);
        if (optionalOwner.isEmpty()) {
            return "redirect:/auth/login";
        }

        PharmacyOwner owner = optionalOwner.get();
        model.addAttribute("owner", owner);

        Optional<Pharmacy> optPharmacy = getActivePharmacy(session, owner);
        if (optPharmacy.isEmpty() && owner.getPharmacies().size() > 1) {
            return "redirect:/owner/pharmacy/select";
        }

        if (optPharmacy.isPresent()) {
            Pharmacy pharmacy = optPharmacy.get();
            model.addAttribute("pharmacy", pharmacy);
            model.addAttribute("orders", pharmacyManagementApi.getRecentOrders(pharmacy.getPharmacyId()));
        }

        return "pharmacy/owner/balance";
    }

    @GetMapping("/settings")
    public String settings(HttpSession session, Model model) {
        Optional<PharmacyOwner> optionalOwner = findAuthenticatedOwner(session);
        if (optionalOwner.isEmpty()) {
            return "redirect:/auth/login";
        }

        PharmacyOwner owner = optionalOwner.get();
        model.addAttribute("owner", owner);

        Optional<Pharmacy> optPharmacy = getActivePharmacy(session, owner);
        if (optPharmacy.isEmpty() && owner.getPharmacies().size() > 1) {
            return "redirect:/owner/pharmacy/select";
        }

        if (optPharmacy.isPresent()) {
            Pharmacy pharmacy = optPharmacy.get();
            model.addAttribute("pharmacy", pharmacy);
        }

        return "pharmacy/owner/settings";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        Optional<PharmacyOwner> optionalOwner = findAuthenticatedOwner(session);
        if (optionalOwner.isEmpty()) {
            return "redirect:/auth/login";
        }

        PharmacyOwner owner = optionalOwner.get();
        model.addAttribute("owner", owner);

        return "pharmacy/owner/profile";
    }

    @GetMapping("/transactions")
    public String transactions(HttpSession session, Model model) {
        Optional<PharmacyOwner> optionalOwner = findAuthenticatedOwner(session);
        if (optionalOwner.isEmpty()) {
            return "redirect:/auth/login";
        }

        PharmacyOwner owner = optionalOwner.get();
        model.addAttribute("owner", owner);

        Optional<Pharmacy> optPharmacy = getActivePharmacy(session, owner);
        if (optPharmacy.isEmpty() && owner.getPharmacies().size() > 1) {
            return "redirect:/owner/pharmacy/select";
        }

        if (optPharmacy.isPresent()) {
            Pharmacy pharmacy = optPharmacy.get();
            model.addAttribute("pharmacy", pharmacy);
            model.addAttribute("orders", pharmacyManagementApi.getRecentOrders(pharmacy.getPharmacyId()));
        }

        return "pharmacy/owner/transactions";
    }

    @PostMapping("/profile/update")
    @Transactional
    public String updateProfile(HttpSession session, @ModelAttribute OwnerProfileDTO profileDTO) {
        Optional<PharmacyOwner> optionalOwner = findAuthenticatedOwner(session);
        if (optionalOwner.isPresent()) {
            pharmacyManagementApi.updateOwnerProfile(optionalOwner.get().getUserId(), profileDTO);
        }
        return "redirect:/owner/pharmacy/profile";
    }

    @PostMapping("/settings/update")
    @Transactional
    public String updateSettings(HttpSession session, @ModelAttribute PharmacySettingsDTO settingsDTO) {
        Optional<PharmacyOwner> optionalOwner = findAuthenticatedOwner(session);
        if (optionalOwner.isPresent()) {
            Optional<Pharmacy> optPharmacy = getActivePharmacy(session, optionalOwner.get());
            if (optPharmacy.isPresent()) {
                Pharmacy pharmacy = optPharmacy.get();
                pharmacyManagementApi.updatePharmacySettings(pharmacy.getPharmacyId(), settingsDTO);
            }
        }
        return "redirect:/owner/pharmacy/settings";
    }

    @PostMapping("/staff/add")
    @Transactional
    public String addStaff(HttpSession session, @ModelAttribute CreatePharmacistRequestDTO dto) {
        Optional<PharmacyOwner> optionalOwner = findAuthenticatedOwner(session);
        if (optionalOwner.isPresent()) {
            Optional<Pharmacy> optPharmacy = getActivePharmacy(session, optionalOwner.get());
            if (optPharmacy.isPresent()) {
                Pharmacy pharmacy = optPharmacy.get();
                try {
                    pharmacyManagementApi.registerPharmacist(pharmacy.getPharmacyId(), dto);
                } catch (Exception e) {
                    // handle error
                }
            }
        }
        return "redirect:/owner/pharmacy/staff";
    }

    @PostMapping("/balance/withdraw")
    @Transactional
    public String withdrawBalance(HttpSession session, @RequestParam("amount") double amount, @RequestParam("bankDetails") String bankDetails) {
        Optional<PharmacyOwner> optionalOwner = findAuthenticatedOwner(session);
        if (optionalOwner.isPresent()) {
            Optional<Pharmacy> optPharmacy = getActivePharmacy(session, optionalOwner.get());
            if (optPharmacy.isPresent()) {
                Pharmacy pharmacy = optPharmacy.get();
                try {
                    pharmacyManagementApi.requestWithdrawal(pharmacy.getPharmacyId(), new WithdrawalRequestDTO(amount, bankDetails));
                } catch (Exception e) {
                    // handle error
                }
            }
        }
        return "redirect:/owner/pharmacy/balance";
    }

    @PostMapping("/staff/edit")
    @Transactional
    public String editStaff(HttpSession session, @RequestParam("pharmacistId") Long pharmacistId, @ModelAttribute com.telemedclinic.pharmacy.internal.dto.UpdatePharmacistRequestDTO dto) {
        Optional<PharmacyOwner> optionalOwner = findAuthenticatedOwner(session);
        if (optionalOwner.isPresent()) {
            try {
                pharmacyManagementApi.updatePharmacist(pharmacistId, dto);
            } catch (Exception e) {
                // handle error
            }
        }
        return "redirect:/owner/pharmacy/staff";
    }
}


