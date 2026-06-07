package com.telemedclinic.customer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.telemedclinic.user.entity.User;
import com.telemedclinic.user.repository.UserRepository;
import com.telemedclinic.cart.repository.CartItemRepository;

import jakarta.servlet.http.HttpSession;

@ControllerAdvice(assignableTypes = CustomerController.class)
public class CustomerControllerAdvice {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @ModelAttribute("cartItemCount")
    public int getCartItemCount(HttpSession session) {
        String roleValue = session.getAttribute("currentUserRole") != null
                ? session.getAttribute("currentUserRole").toString()
                : null;
        
        if ("ROLE_CUSTOMER".equals(roleValue) || "CUSTOMER".equals(roleValue)) {
            String email = (String) session.getAttribute("currentUserEmail");
            if (email != null) {
                User user = userRepository.findByEmail(email).orElse(null);
                if (user != null) {
                    return (int) cartItemRepository.countByCustomerUserId(user.getUserId());
                }
            }
        }
        return 0;
    }
}
