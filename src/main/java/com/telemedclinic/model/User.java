package com.telemedclinic.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User {

    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;


    // No-args constructor for JPA
    public User() {
    }


    // Constructor
    public User(
            String name,
            String email,
            String password,
            Role role
    ) {

        setName(name);
        setEmail(email);
        setPassword(password);
        setRole(role);
    }


    // Getter
    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    protected String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    // Setter
    public void setName(String name) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Name cannot be empty."
            );
        }

        this.name = name;
    }

    public void setEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email cannot be empty."
            );
        }

        if (!email.contains("@")) {
            throw new IllegalArgumentException(
                    "Email must contain @."
            );
        }

        this.email = email;
    }

    public void setPassword(String password) {

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Password cannot be empty."
            );
        }

        this.password = password;
    }

    public void setRole(Role role) {

        if (role == null) {
            throw new IllegalArgumentException(
                    "Role cannot be null."
            );
        }

        this.role = role;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @PrePersist
    protected void setCreatedAtBeforePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }


    // Behavior methods
    public void updateProfile(String name) {
        setName(name);
    }

    public void changePassword(String newPassword) {
        setPassword(newPassword);
    }

    public void activate() {
        setActive(true);
    }

    public void deactivate() {
        setActive(false);
    }

    public void toggleActiveStatus() {
        setActive(!active);
    }

    public boolean matchesPassword(
            String rawPassword,
            PasswordEncoder passwordEncoder
    ) {

        if (rawPassword == null || passwordEncoder == null) {
            return false;
        }

        return passwordEncoder.matches(rawPassword, this.password);
    }
}
