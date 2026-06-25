package com.telemedclinic.delivery.core.port.out;

import com.telemedclinic.delivery.core.domain.MockDeliveryStatus;

public interface DeliveryServicePort {
    /**
     * Meminta pengiriman untuk sebuah order.
     * @param orderId ID pesanan
     * @param destinationAddress Alamat tujuan
     * @return trackingId atau resi pengiriman
     */
    String requestDelivery(Long orderId, String destinationAddress);

    /**
     * Mengecek status pengiriman.
     * @param trackingId ID tracking
     * @return Status saat ini
     */
    MockDeliveryStatus checkStatus(String trackingId);
}
