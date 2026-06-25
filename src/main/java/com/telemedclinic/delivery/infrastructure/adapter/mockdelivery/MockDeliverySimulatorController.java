package com.telemedclinic.delivery.infrastructure.adapter.mockdelivery;

import com.telemedclinic.delivery.core.domain.MockDeliveryStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/mock-delivery")
public class MockDeliverySimulatorController {

    private final MockDeliveryRepository deliveryRepository;

    public MockDeliverySimulatorController(MockDeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    @PostMapping("/{trackingId}/advance")
    @Transactional
    public ResponseEntity<String> advanceStatus(@PathVariable String trackingId) {
        MockDeliveryEntity delivery = deliveryRepository.findById(trackingId)
                .orElse(null);

        if (delivery == null) {
            return ResponseEntity.notFound().build();
        }

        MockDeliveryStatus currentStatus = delivery.getStatus();
        MockDeliveryStatus nextStatus;

        switch (currentStatus) {
            case WAITING_FOR_DRIVER:
                nextStatus = MockDeliveryStatus.DRIVER_HEADING_TO_PHARMACY;
                break;
            case DRIVER_HEADING_TO_PHARMACY:
                nextStatus = MockDeliveryStatus.DRIVER_ARRIVED_AT_PHARMACY;
                break;
            case DRIVER_ARRIVED_AT_PHARMACY:
                nextStatus = MockDeliveryStatus.ON_THE_WAY_TO_CUSTOMER;
                break;
            case ON_THE_WAY_TO_CUSTOMER:
                nextStatus = MockDeliveryStatus.DELIVERED;
                break;
            case DELIVERED:
                return ResponseEntity.badRequest().body("Pengiriman sudah selesai (DELIVERED).");
            default:
                throw new IllegalStateException("Status tidak valid: " + currentStatus);
        }

        delivery.setStatus(nextStatus);
        deliveryRepository.save(delivery);

        return ResponseEntity.ok("Status pengiriman " + trackingId + " diperbarui menjadi: " + nextStatus);
    }
}
