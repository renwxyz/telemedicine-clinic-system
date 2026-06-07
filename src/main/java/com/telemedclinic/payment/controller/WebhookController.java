package com.telemedclinic.payment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telemedclinic.order.entity.Order;
import com.telemedclinic.order.entity.OrderStatus;
import com.telemedclinic.order.entity.PaymentStatus;
import com.telemedclinic.order.repository.OrderRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;

@RestController
@RequestMapping("/api/midtrans")
public class WebhookController {

    @Value("${midtrans.server.key}")
    private String serverKey;

    @Autowired
    private OrderRepository orderRepository;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(payload);

            String orderId = jsonNode.path("order_id").asText();
            String statusCode = jsonNode.path("status_code").asText();
            String grossAmount = jsonNode.path("gross_amount").asText();
            String signatureKey = jsonNode.path("signature_key").asText();
            String transactionStatus = jsonNode.path("transaction_status").asText();

            // Verify signature
            String rawSignature = orderId + statusCode + grossAmount + serverKey;
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hashBytes = digest.digest(rawSignature.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            if (!hexString.toString().equals(signatureKey)) {
                System.err.println("Midtrans Signature verification failed for order " + orderId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Signature");
            }

            // Process status
            if ("settlement".equals(transactionStatus) || "capture".equals(transactionStatus)) {
                Optional<Order> orderOpt = orderRepository.findByOrderId(orderId);
                if (orderOpt.isPresent()) {
                    Order order = orderOpt.get();
                    if (order.getPaymentStatus() != PaymentStatus.PAID) {
                        order.setPaymentStatus(PaymentStatus.PAID);
                        order.setStatus(OrderStatus.PROCESSING);
                        orderRepository.save(order);
                        System.out.println("Midtrans Webhook: Order " + orderId + " marked as PAID/PROCESSING");
                    }
                } else {
                    System.err.println("Midtrans Webhook: Order not found: " + orderId);
                }
            } else if ("cancel".equals(transactionStatus) || "deny".equals(transactionStatus) || "expire".equals(transactionStatus)) {
                Optional<Order> orderOpt = orderRepository.findByOrderId(orderId);
                if (orderOpt.isPresent()) {
                    Order order = orderOpt.get();
                    order.setPaymentStatus(PaymentStatus.FAILED);
                    order.setStatus(OrderStatus.CANCELLED);
                    orderRepository.save(order);
                    System.out.println("Midtrans Webhook: Order " + orderId + " marked as FAILED/CANCELLED");
                }
            }

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            System.err.println("Error processing Midtrans webhook");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook");
        }
    }
}
