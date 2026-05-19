package com.telemedclinic.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.telemedclinic.dto.CreatePharmacistAccountRequest;
import com.telemedclinic.dto.CustomerRegisterRequest;
import com.telemedclinic.dto.DoctorRegisterRequest;
import com.telemedclinic.dto.PharmacyRegisterRequest;
import com.telemedclinic.model.Pharmacy;
import com.telemedclinic.service.AuthService;
import com.telemedclinic.service.PharmacyService;

@Controller
@RequestMapping("/register")
public class RegistrationPageController {

    private final AuthService authService;
    private final PharmacyService pharmacyService;

    public RegistrationPageController(
            AuthService authService,
            PharmacyService pharmacyService
    ) {

        this.authService = authService;
        this.pharmacyService = pharmacyService;
    }

    @GetMapping
    public String showRegistrationPage(Model model) {
        addEmptyForms(model);
        return "auth/register";
    }

    @PostMapping("/customer")
    public String registerCustomer(
            @ModelAttribute("customerRequest") CustomerRegisterRequest request,
            Model model
    ) {

        try {
            authService.registerCustomer(request);
            model.addAttribute("message", "Customer registered successfully.");
        } catch (RuntimeException exception) {
            model.addAttribute("error", exception.getMessage());
        }

        addEmptyForms(model);
        return "auth/register";
    }

    @PostMapping("/doctor")
    public String registerDoctor(
            @ModelAttribute("doctorRequest") DoctorRegisterRequest request,
            Model model
    ) {

        try {
            authService.registerDoctor(request);
            model.addAttribute("message", "Doctor registered successfully.");
        } catch (RuntimeException exception) {
            model.addAttribute("error", exception.getMessage());
        }

        addEmptyForms(model);
        return "auth/register";
    }

    @PostMapping("/pharmacy")
    public String registerPharmacy(
            @ModelAttribute("pharmacyRequest") PharmacyRegisterRequest request,
            Model model
    ) {

        try {
            Pharmacy pharmacy = pharmacyService.registerPharmacy(request);
            model.addAttribute(
                    "message",
                    "Pharmacy registered successfully with ID: " + pharmacy.getPharmacyId()
            );
        } catch (RuntimeException exception) {
            model.addAttribute("error", exception.getMessage());
        }

        addEmptyForms(model);
        return "auth/register";
    }

    @PostMapping("/pharmacist")
    public String createPharmacistAccount(
            @ModelAttribute("pharmacistRequest") CreatePharmacistAccountRequest request,
            Model model
    ) {

        try {
            authService.createPharmacistAccount(request);
            model.addAttribute("message", "Pharmacist account created successfully.");
        } catch (RuntimeException exception) {
            model.addAttribute("error", exception.getMessage());
        }

        addEmptyForms(model);
        return "auth/register";
    }

    private void addEmptyForms(Model model) {
        model.addAttribute("customerRequest", new CustomerRegisterRequest());
        model.addAttribute("doctorRequest", new DoctorRegisterRequest());
        model.addAttribute("pharmacyRequest", new PharmacyRegisterRequest());
        model.addAttribute("pharmacistRequest", new CreatePharmacistAccountRequest());
    }
}
