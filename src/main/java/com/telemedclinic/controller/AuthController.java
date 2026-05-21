package com.telemedclinic.controller;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.telemedclinic.dto.CustomerRegisterForm;
import com.telemedclinic.dto.CustomerRegisterRequest;
import com.telemedclinic.dto.LoginForm;
import com.telemedclinic.dto.LoginRequest;
import com.telemedclinic.dto.AuthResponse;
import com.telemedclinic.model.Role;
import com.telemedclinic.service.AuthService;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Menampilkan halaman login.
    @GetMapping("/login")
    public String showLoginPage(
            @RequestParam(value = "registered", required = false) Boolean registered,
            Model model
    ) {

        model.addAttribute("loginForm", new LoginForm());

        if (Boolean.TRUE.equals(registered)) {
            model.addAttribute("successMessage", "Registrasi berhasil! Silakan masuk.");
        }

        return "auth/login";
    }

    // Memproses submit form login dan mengarahkan user sesuai role.
    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute("loginForm") LoginForm loginForm,
            BindingResult bindingResult,
            HttpSession session,
            Model model
    ) {

        if (bindingResult.hasErrors()) {
            return "auth/login";
        }

        try {
            AuthResponse authResponse = authService.login(
                    new LoginRequest(
                            loginForm.getEmail(),
                            loginForm.getPassword()
                    )
            );

            session.setAttribute("currentUserId", authResponse.getUserId());
            session.setAttribute("currentUserName", authResponse.getName());
            session.setAttribute("currentUserEmail", authResponse.getEmail());
            session.setAttribute("currentUserRole", authResponse.getRole());

            if (authResponse.getRole() == Role.ROLE_ADMIN) {
                return "redirect:/admin/dashboard";
            }

            if (authResponse.getRole() == Role.ROLE_PHARMACIST) {
                return "redirect:/pharmacist/dashboard";
            }

            if (authResponse.getRole() == Role.ROLE_DOCTOR) {
                return "redirect:/doctor/dashboard";
            }

            return "redirect:/customer/dashboard";
        } catch (RuntimeException exception) {
            model.addAttribute("error", "Email atau password salah.");
            return "auth/login";
        }
    }

    // Menghapus session login.
    @GetMapping("/logout")
    public String logoutWithGet(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login?logout=true";
    }

    // Menghapus session login.
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login?logout=true";
    }

    // Menampilkan halaman registrasi customer.
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("customerRegisterForm", new CustomerRegisterForm());
        return "auth/register";
    }

    // Memproses submit form registrasi customer.
    @PostMapping("/register/customer")
    public String registerCustomer(
            @Valid @ModelAttribute("customerRegisterForm") CustomerRegisterForm customerRegisterForm,
            BindingResult bindingResult,
            Model model
    ) {

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            authService.registerCustomer(
                    new CustomerRegisterRequest(
                            customerRegisterForm.getName(),
                            customerRegisterForm.getEmail(),
                            customerRegisterForm.getPassword(),
                            customerRegisterForm.getPhoneNumber(),
                            customerRegisterForm.getAddress(),
                            customerRegisterForm.getGender(),
                            customerRegisterForm.getBirthDate(),
                            customerRegisterForm.getHeight(),
                            customerRegisterForm.getWeight()
                    )
            );

            return "redirect:/auth/login?registered=true";
        } catch (RuntimeException exception) {
            model.addAttribute("error", exception.getMessage());
            return "auth/register";
        }
    }
}
