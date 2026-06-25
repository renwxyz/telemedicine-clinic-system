package com.telemedclinic.payment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.telemedclinic.order.entity.Order;
import com.telemedclinic.consultation.model.Consultation;

import java.util.Base64;

@Service
public class MidtransService {

    @Value("${midtrans.server.key}")
    private String serverKey;

    @Value("${midtrans.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public MidtransService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String createTransaction(Order order) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString((serverKey + ":").getBytes()));

            // Build JSON payload
            ObjectNode requestBody = objectMapper.createObjectNode();

            ObjectNode transactionDetails = objectMapper.createObjectNode();
            
            // PERBAIKAN: Tambahkan timestamp agar order_id SELALU UNIK di mata Midtrans
            String uniqueOrderId = "TC-" + order.getOrderId() + "-" + System.currentTimeMillis();
            transactionDetails.put("order_id", uniqueOrderId);
            
            transactionDetails.put("gross_amount", (int) Math.round(order.getTotalAmount()));
            requestBody.set("transaction_details", transactionDetails);

            ObjectNode customerDetails = objectMapper.createObjectNode();
            customerDetails.put("first_name", order.getRecipientName());
            customerDetails.put("phone", order.getRecipientPhone());
            if (order.getCustomer() != null) {
                customerDetails.put("email", order.getCustomer().getEmail());
            }
            requestBody.set("customer_details", customerDetails);

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ObjectNode jsonResponse = (ObjectNode) objectMapper.readTree(response.getBody());
                return jsonResponse.get("token").asText();
            }

        } catch (Exception e) {
            // PERBAIKAN: Log Error yang jauh lebih mencolok agar gampang dicari
            System.err.println("=============================================");
            System.err.println("❌ GAGAL MENDAPATKAN TOKEN MIDTRANS");
            System.err.println("Penyebab Error: " + e.getMessage());
            System.err.println("=============================================");
            e.printStackTrace();
        }
        return null;
    }

    public String createConsultationTransaction(Consultation consultation) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString((serverKey + ":").getBytes()));

            // Build JSON payload
            ObjectNode requestBody = objectMapper.createObjectNode();

            ObjectNode transactionDetails = objectMapper.createObjectNode();
            
            String uniqueOrderId = "CONS-" + consultation.getId() + "-" + System.currentTimeMillis();
            transactionDetails.put("order_id", uniqueOrderId);
            
            double fee = consultation.getDoctor() != null && consultation.getDoctor().getConsultationFee() != null
                    ? consultation.getDoctor().getConsultationFee()
                    : 50000.0;
            transactionDetails.put("gross_amount", (int) fee);
            requestBody.set("transaction_details", transactionDetails);

            ObjectNode customerDetails = objectMapper.createObjectNode();
            customerDetails.put("first_name", consultation.getCustomer().getName());
            customerDetails.put("phone", consultation.getCustomer().getPhoneNumber());
            customerDetails.put("email", consultation.getCustomer().getEmail());
            requestBody.set("customer_details", customerDetails);

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ObjectNode jsonResponse = (ObjectNode) objectMapper.readTree(response.getBody());
                return jsonResponse.get("token").asText();
            }

        } catch (Exception e) {
            System.err.println("=============================================");
            System.err.println("❌ GAGAL MENDAPATKAN TOKEN KONSULTASI MIDTRANS");
            System.err.println("Penyebab Error: " + e.getMessage());
            System.err.println("=============================================");
            e.printStackTrace();
        }
        return null;
    }
}