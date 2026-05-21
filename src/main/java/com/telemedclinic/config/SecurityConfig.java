package com.telemedclinic.config;

import jakarta.servlet.http.HttpSession;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import com.telemedclinic.model.Role;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/auth/**",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()
                        .requestMatchers("/admin/**").access(
                                (authentication, context) -> hasSessionRole(context, Role.ROLE_ADMIN)
                        )
                        .requestMatchers("/pharmacist/**").access(
                                (authentication, context) -> hasSessionRole(context, Role.ROLE_PHARMACIST)
                        )
                        .requestMatchers("/customer/**").access(
                                (authentication, context) -> hasSessionRole(context, Role.ROLE_CUSTOMER)
                        )
                        .requestMatchers("/doctor/**").access(
                                (authentication, context) -> hasSessionRole(context, Role.ROLE_DOCTOR)
                        )
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .build();
    }

    private AuthorizationDecision hasSessionRole(
            RequestAuthorizationContext context,
            Role requiredRole
    ) {

        HttpSession session = context.getRequest().getSession(false);
        boolean granted = session != null && requiredRole.equals(session.getAttribute("currentUserRole"));

        return new AuthorizationDecision(granted);
    }
}
