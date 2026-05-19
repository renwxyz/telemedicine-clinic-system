package com.telemedclinic.dto;

import com.telemedclinic.model.Role;

public class AuthResponse {

    private Long userId;
    private String name;
    private String email;
    private Role role;

    public AuthResponse(Long userId, String name, String email, Role role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }
}
