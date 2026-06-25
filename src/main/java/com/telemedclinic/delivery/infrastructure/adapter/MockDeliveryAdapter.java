package com.telemedclinic.delivery.infrastructure.adapter;

import com.telemedclinic.delivery.core.domain.MockDeliveryStatus;
import com.telemedclinic.delivery.core.port.out.DeliveryServicePort;
import com.telemedclinic.delivery.infrastructure.adapter.mockdelivery.MockDeliveryEntity;
import com.telemedclinic.delivery.infrastructure.adapter.mockdelivery.MockDeliveryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MockDeliveryAdapter implements DeliveryServicePort {

    private final MockDeliveryRepository deliveryRepository;

    public MockDeliveryAdapter(MockDeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    @Override
    @Transactional
    public String requestDelivery(Long orderId, String destinationAddress) {
        // Generate a random tracking ID
        String trackingId = "DELV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // Pick a random driver name for realism
        String[] drivers = {"Budi Santoso", "Andi Pratama", "Siti Aminah", "Eko Supriyanto", "Joko Widodo"};
        String driverName = drivers[(int) (Math.random() * drivers.length)];

        MockDeliveryEntity delivery = new MockDeliveryEntity(
                trackingId,
                orderId,
                destinationAddress,
                MockDeliveryStatus.WAITING_FOR_DRIVER,
                driverName
        );

        deliveryRepository.save(delivery);
        
        return trackingId;
    }

    @Override
    @Transactional(readOnly = true)
    public MockDeliveryStatus checkStatus(String trackingId) {
        return deliveryRepository.findById(trackingId)
                .map(MockDeliveryEntity::getStatus)
                .orElseThrow(() -> new IllegalArgumentException("Tracking ID tidak ditemukan: " + trackingId));
    }
}
