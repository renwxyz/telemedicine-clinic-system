package com.telemedclinic.config;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import com.telemedclinic.user.entity.Role;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/midtrans/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/auth/**",
                                "/auth/change-password",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()
                        .requestMatchers("/admin/**").access(
                                (authentication, context) -> hasSessionRole(context, Role.ROLE_ADMIN)
                        )
                        .requestMatchers("/pharmacist/**").access(
                                (authentication, context) -> hasPharmacistAccess(context)
                        )
                        .requestMatchers("/customer/**").access(
                                (authentication, context) -> hasSessionRole(context, Role.ROLE_CUSTOMER)
                        )
                        .requestMatchers("/doctor/**").access(
                                (authentication, context) -> hasDoctorAccess(context)
                        )
                        .anyRequest().permitAll()
                )
                .exceptionHandling(exception -> exception.accessDeniedHandler((request, response, accessDeniedException) -> {
                    HttpSession session = request.getSession(false);

                    if (request.getRequestURI().startsWith("/doctor/")
                            && session != null
                            && Role.ROLE_DOCTOR.equals(session.getAttribute("currentUserRole"))
                            && Boolean.TRUE.equals(session.getAttribute("mustChangePassword"))) {
                        response.sendRedirect("/auth/change-password");
                        return;
                    }

                    if (request.getRequestURI().startsWith("/pharmacist/")
                            && session != null
                            && Role.ROLE_PHARMACIST.equals(session.getAttribute("currentUserRole"))
                            && Boolean.TRUE.equals(session.getAttribute("mustChangePassword"))) {
                        response.sendRedirect("/auth/change-password");
                        return;
                    }

                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }))
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .build();
    }

    private AuthorizationDecision hasSessionRole(
            RequestAuthorizationContext context,
            Role requiredRole
    ) {

        HttpSession session = context.getRequest().getSession(false);
        boolean granted = session != null
                && session.getAttribute("currentUserRole") != null
                && requiredRole.name().equals(session.getAttribute("currentUserRole").toString());

        return new AuthorizationDecision(granted);
    }

    private AuthorizationDecision hasDoctorAccess(RequestAuthorizationContext context) {
        HttpSession session = context.getRequest().getSession(false);
        boolean granted = session != null
                && Role.ROLE_DOCTOR.equals(session.getAttribute("currentUserRole"))
                && !Boolean.TRUE.equals(session.getAttribute("mustChangePassword"));

        return new AuthorizationDecision(granted);
    }

    private AuthorizationDecision hasPharmacistAccess(RequestAuthorizationContext context) {
        HttpSession session = context.getRequest().getSession(false);
        boolean granted = session != null
                && Role.ROLE_PHARMACIST.equals(session.getAttribute("currentUserRole"))
                && !Boolean.TRUE.equals(session.getAttribute("mustChangePassword"));

        return new AuthorizationDecision(granted);
    }
}
