package com.telemedclinic.delivery.infrastructure.adapter.mockdelivery;

import com.telemedclinic.delivery.core.domain.MockDeliveryStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mock_deliveries")
public class MockDeliveryEntity {

    @Id
    private String trackingId;

    private Long orderId;

    private String destinationAddress;

    @Enumerated(EnumType.STRING)
    private MockDeliveryStatus status;

    private String driverName;

    // Constructors
    public MockDeliveryEntity() {
    }

    public MockDeliveryEntity(String trackingId, Long orderId, String destinationAddress, MockDeliveryStatus status, String driverName) {
        this.trackingId = trackingId;
        this.orderId = orderId;
        this.destinationAddress = destinationAddress;
        this.status = status;
        this.driverName = driverName;
    }

    // Getters and Setters
    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public MockDeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(MockDeliveryStatus status) {
        this.status = status;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }
}
