package com.telemedclinic.order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.telemedclinic.order.entity.Order;
import com.telemedclinic.order.entity.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerUserIdOrderByCreatedAtDesc(Long customerId);
    List<Order> findByCustomerUserIdAndStatusOrderByCreatedAtDesc(Long customerId, OrderStatus status);
    List<Order> findByStatus(OrderStatus status);
    java.util.Optional<Order> findByOrderId(String orderId);
    List<Order> findByPharmacyIdOrderByCreatedAtDesc(Long pharmacyId);
    List<Order> findByStatusAndPrescriptionIdIsNotNull(OrderStatus status);
}
