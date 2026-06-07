package com.telemedclinic.order.entity;

public enum OrderStatus {
    PENDING,
    PENDING_PHARMACY_CONFIRMATION,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    COMPLETED,
    CANCELLED
}
