package com.telemedclinic.cart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.telemedclinic.cart.entity.CartItem;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCustomerUserId(Long customerId);
    Optional<CartItem> findByCustomerUserIdAndInventoryItemInventoryItemId(Long customerId, Long inventoryItemId);
    long countByCustomerUserId(Long customerId);
    void deleteByCustomerUserId(Long customerId);
}
