package com.telemedclinic.delivery.infrastructure.adapter.mockdelivery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MockDeliveryRepository extends JpaRepository<MockDeliveryEntity, String> {
}
