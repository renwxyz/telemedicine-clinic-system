package com.telemedclinic.auth.service;

import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String fromAddress;

    public EmailService(
            JavaMailSender mailSender,
            SpringTemplateEngine templateEngine
    ) {

        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public EmailResult sendDoctorCredentials(
            String toEmail,
            String doctorName,
            String tempPassword
    ) {

        try {
            Context context = new Context();
            context.setVariable("doctorName", doctorName);
            context.setVariable("email", toEmail);
            context.setVariable("tempPassword", tempPassword);

            String htmlContent = templateEngine.process(
                    "auth/doctor-credentials-email",
                    context
            );

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Kredensial Akun Telemedclinic Anda");
            helper.setText(htmlContent, true);

            mailSender.send(message);

            return EmailResult.success();
        } catch (Exception exception) {
            logger.error("Failed to send doctor credentials email to {}.", toEmail, exception);
            return EmailResult.failure(exception.getMessage());
        }
    }

    public EmailResult sendPharmacistCredentials(
            String toEmail,
            String pharmacistName,
            String tempPassword
    ) {

        try {
            Context context = new Context();
            context.setVariable("pharmacistName", pharmacistName);
            context.setVariable("email", toEmail);
            context.setVariable("tempPassword", tempPassword);

            String htmlContent = templateEngine.process(
                    "auth/pharmacist-credentials-email",
                    context
            );

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Kredensial Akun Apoteker Telemedclinic Anda");
            helper.setText(htmlContent, true);

            mailSender.send(message);

            return EmailResult.success();
        } catch (Exception exception) {
            logger.error("Failed to send pharmacist credentials email to {}.", toEmail, exception);
            return EmailResult.failure(exception.getMessage());
        }
    }

    public EmailResult sendOwnerCredentials(
            String toEmail,
            String ownerName,
            String tempPassword
    ) {
        try {
            Context context = new Context();
            context.setVariable("ownerName", ownerName);
            context.setVariable("email", toEmail);
            context.setVariable("tempPassword", tempPassword);

            String htmlContent = templateEngine.process(
                    "auth/owner-credentials-email",
                    context
            );

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Kredensial Akun Pemilik Apotek Telemedclinic Anda");
            helper.setText(htmlContent, true);

            mailSender.send(message);

            return EmailResult.success();
        } catch (Exception exception) {
            logger.error("Failed to send owner credentials email to {}.", toEmail, exception);
            return EmailResult.failure(exception.getMessage());
        }
    }
}
