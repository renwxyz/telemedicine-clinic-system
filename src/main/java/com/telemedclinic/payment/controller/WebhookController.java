package com.telemedclinic.payment.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.telemedclinic.order.entity.Order;
import com.telemedclinic.order.entity.OrderStatus;
import com.telemedclinic.order.entity.PaymentStatus;
import com.telemedclinic.order.repository.OrderRepository;

@RestController
@RequestMapping("/api/midtrans")
public class WebhookController {

    @Autowired
    private OrderRepository orderRepository;

    @Value("${midtrans.server.key}")
    private String serverKey;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> payload) {
        try {
            // 3. EKSTRAK DATA DARI PAYLOAD
            String orderId = String.valueOf(payload.get("order_id"));
            String statusCode = String.valueOf(payload.get("status_code"));
            
            Object grossAmountObj = payload.get("gross_amount");
            String grossAmount = String.valueOf(grossAmountObj);
            if (!grossAmount.contains(".")) {
                grossAmount += ".00";
            } else if (grossAmount.endsWith(".0")) {
                grossAmount += "0";
            }
            
            String transactionStatus = String.valueOf(payload.get("transaction_status"));
            String signatureKey = String.valueOf(payload.get("signature_key"));

            // 4. VALIDASI KEAMANAN (SHA-512)
            String payloadToHash = orderId + statusCode + grossAmount + serverKey;
            
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(payloadToHash.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            String calculatedSignature = hexString.toString();

            System.out.println("Midtrans Webhook Received!");
            System.out.println("Payload to hash: " + payloadToHash);
            System.out.println("Calculated Signature: " + calculatedSignature);
            System.out.println("Midtrans Signature: " + signatureKey);

            if (!calculatedSignature.equalsIgnoreCase(signatureKey)) {
                System.err.println("❌ SIGNATURE MISMATCH!");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
            }

            System.out.println("✅ SIGNATURE MATCH! Processing order...");

            // 5. EKSTRAK ID PESANAN ASLI
            String[] parts = orderId.split("-");
            if (parts.length >= 3) {
                String originalOrderId = parts[1] + "-" + parts[2];

                // 6. UPDATE DATABASE
                Optional<Order> optionalOrder = orderRepository.findByOrderId(originalOrderId);
                if (optionalOrder.isPresent()) {
                    Order order = optionalOrder.get();
                    if ("settlement".equals(transactionStatus) || "capture".equals(transactionStatus)) {
                        order.setPaymentStatus(PaymentStatus.PAID);
                        order.setStatus(OrderStatus.PROCESSING);
                    } else if ("cancel".equals(transactionStatus) || "deny".equals(transactionStatus) || "expire".equals(transactionStatus)) {
                        order.setStatus(OrderStatus.CANCELLED);
                    } else if ("pending".equals(transactionStatus)) {
                        order.setPaymentStatus(PaymentStatus.PENDING);
                    }
                    orderRepository.save(order);
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 7. KEMBALIKAN RESPONS 200 OK APAPUN YANG TERJADI
        return ResponseEntity.ok("OK");
    }
}