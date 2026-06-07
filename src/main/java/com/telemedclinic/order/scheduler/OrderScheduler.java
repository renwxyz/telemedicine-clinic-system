package com.telemedclinic.order.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.telemedclinic.finance.service.FinanceService;
import com.telemedclinic.order.entity.Order;
import com.telemedclinic.order.entity.OrderStatus;
import com.telemedclinic.order.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderScheduler {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private FinanceService financeService;

    @Scheduled(fixedRate = 60000)
    public void processOrders() {
        System.out.println("Scheduler running: Checking orders...");

        List<Order> processingOrders = orderRepository.findByStatus(OrderStatus.PROCESSING);

        LocalDateTime now = LocalDateTime.now();

        for (Order order : processingOrders) {
            LocalDateTime refTime = order.getUpdatedAt() != null ? order.getUpdatedAt() : order.getCreatedAt();
            
            if (refTime != null) {
                if (now.isAfter(refTime.plusMinutes(20))) {
                    // Update to DELIVERED
                    order.setStatus(OrderStatus.DELIVERED);
                    orderRepository.save(order);
                    System.out.println("Order " + order.getOrderId() + " updated to DELIVERED.");
                    
                    // Release funds
                    financeService.releaseFundsToPharmacy(order);
                    
                } else if (now.isAfter(refTime.plusMinutes(10))) {
                    // Update to SHIPPED
                    order.setStatus(OrderStatus.SHIPPED);
                    orderRepository.save(order);
                    System.out.println("Order " + order.getOrderId() + " updated to SHIPPED.");
                }
            }
        }
        
        // Also process SHIPPED orders to DELIVERED if they stuck in SHIPPED status
        List<Order> shippedOrders = orderRepository.findByStatus(OrderStatus.SHIPPED);
        for (Order order : shippedOrders) {
            LocalDateTime refTime = order.getUpdatedAt() != null ? order.getUpdatedAt() : order.getCreatedAt();
            if (refTime != null && now.isAfter(refTime.plusMinutes(10))) {
                order.setStatus(OrderStatus.DELIVERED);
                orderRepository.save(order);
                System.out.println("Order " + order.getOrderId() + " updated from SHIPPED to DELIVERED.");
                financeService.releaseFundsToPharmacy(order);
            }
        }
    }
}
