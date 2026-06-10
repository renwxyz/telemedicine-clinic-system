package com.telemedclinic.payment.controller;

import com.telemedclinic.order.entity.Order;
import com.telemedclinic.order.entity.OrderStatus;
import com.telemedclinic.order.entity.PaymentStatus;
import com.telemedclinic.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.MessageDigest;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/midtrans")
public class WebhookController {

    private final OrderRepository orderRepository;

    @Value("${midtrans.server.key}")
    private String serverKey;

    public WebhookController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleNotification(@RequestBody Map<String, Object> payload) {
        try {
            // Gunakan String.valueOf untuk menghindari ClassCastException JSON Jackson
            String orderIdStr = String.valueOf(payload.get("order_id"));
            String statusCode = String.valueOf(payload.get("status_code"));
            String grossAmount = String.valueOf(payload.get("gross_amount"));
            String signatureKey = String.valueOf(payload.get("signature_key"));
            String transactionStatus = String.valueOf(payload.get("transaction_status"));

            // Aturan Emas 3: VALIDASI KEAMANAN SHA-512
            String rawString = orderIdStr + statusCode + grossAmount + serverKey;
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(rawString.getBytes("UTF-8"));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            String calculatedSignature = hexString.toString();

            if (!calculatedSignature.equalsIgnoreCase(signatureKey)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Tanda tangan (Signature) tidak valid!");
            }

            // Ekstrak ID Order asli dari format "TC-{id}-{timestamp}" yang kita buat tadi
            Long actualOrderId = Long.parseLong(orderIdStr.split("-")[1]);
            Optional<Order> optionalOrder = orderRepository.findById(actualOrderId);

            if (optionalOrder.isPresent()) {
                Order order = optionalOrder.get();

                // UPDATE STATUS BERDASARKAN RESPON MIDTRANS
                if (transactionStatus.equals("settlement") || transactionStatus.equals("capture")) {
                    order.setPaymentStatus(PaymentStatus.PAID);
                    order.setStatus(OrderStatus.PROCESSING); // Order langsung berpindah ke fase disiapkan oleh Apotek
                } else if (transactionStatus.equals("expire") || transactionStatus.equals("cancel") || transactionStatus.equals("deny")) {
                    order.setStatus(OrderStatus.CANCELLED);
                }
                
                orderRepository.save(order);
            }

            // WAJIB MERESPON 200 OK AGAR MIDTRANS BERHENTI MENGIRIM NOTIFIKASI
            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Terjadi kesalahan internal server.");
        }
    }
}