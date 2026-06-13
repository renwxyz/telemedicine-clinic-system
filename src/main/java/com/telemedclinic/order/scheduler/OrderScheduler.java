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
                } 
                // Auto-shipped is disabled because Pharmacist will do it manually
            }
        }
        
        // Also process SHIPPED orders to DELIVERED if they stuck in SHIPPED status
        List<Order> shippedOrders = orderRepository.findByStatus(OrderStatus.SHIPPED);
        for (Order order : shippedOrders) {
            LocalDateTime refTime = order.getUpdatedAt() != null ? order.getUpdatedAt() : order.getCreatedAt();
            // [UBAH WAKTU DI SINI] - Ganti angka 1 menjadi 10, 30, atau 60 (dalam hitungan menit) untuk mengatur lama pengiriman kurir.
            if (refTime != null && now.isAfter(refTime.plusMinutes(1))) {
                order.setStatus(OrderStatus.DELIVERED);
                orderRepository.save(order);
                System.out.println("Order " + order.getOrderId() + " updated from SHIPPED to DELIVERED.");
                financeService.releaseFundsToPharmacy(order);
            }
        }
    }
}
