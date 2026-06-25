package com.telemedclinic.pharmacy.internal.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.telemedclinic.pharmacy.api.PharmacyManagementApi;
import com.telemedclinic.pharmacy.internal.dto.OwnerProfileDTO;
import com.telemedclinic.pharmacy.internal.dto.PharmacySettingsDTO;
import com.telemedclinic.pharmacy.internal.entity.PharmacyOwner;
import com.telemedclinic.pharmacy.internal.entity.Pharmacy;
import com.telemedclinic.pharmacy.internal.repository.PharmacyOwnerRepository;
import com.telemedclinic.pharmacy.internal.repository.PharmacyRepository;
import com.telemedclinic.pharmacy.internal.dto.CreatePharmacistRequestDTO;
import com.telemedclinic.pharmacy.internal.dto.WithdrawalRequestDTO;
import com.telemedclinic.user.dto.CreatePharmacistRequest;
import com.telemedclinic.auth.service.PharmacistProvisioningService;
import com.telemedclinic.auth.service.PharmacistProvisioningResult;
import com.telemedclinic.user.repository.UserRepository;
import com.telemedclinic.order.repository.OrderRepository;
import com.telemedclinic.order.entity.Order;
import java.time.LocalDate;

@Service
@Transactional
public class PharmacyService implements PharmacyManagementApi {

    private final PharmacyOwnerRepository ownerRepository;
    private final PharmacistProvisioningService pharmacistProvisioningService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PharmacyRepository pharmacyRepository;

    public PharmacyService(PharmacyOwnerRepository ownerRepository, PharmacyRepository pharmacyRepository, PharmacistProvisioningService pharmacistProvisioningService, UserRepository userRepository, OrderRepository orderRepository) {
        this.ownerRepository = ownerRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.pharmacistProvisioningService = pharmacistProvisioningService;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public void updateOwnerProfile(Long ownerId, OwnerProfileDTO profileDTO) {
        PharmacyOwner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found"));
        
        owner.setName(profileDTO.name());
        owner.setPhoneNumber(profileDTO.phoneNumber());
        ownerRepository.save(owner);
    }

    @Override
    public void updatePharmacySettings(Long pharmacyId, PharmacySettingsDTO settingsDTO) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new IllegalArgumentException("Pharmacy not found"));
        
        pharmacy.setName(settingsDTO.name());
        pharmacy.setAddress(settingsDTO.address());
        pharmacy.setPhoneNumber(settingsDTO.phoneNumber());
        pharmacy.setLegalDocumentNumber(settingsDTO.legalDocumentNumber());
        pharmacy.setLatitude(settingsDTO.latitude());
        pharmacy.setLongitude(settingsDTO.longitude());
        
        pharmacyRepository.save(pharmacy);
    }

    public Pharmacy registerPharmacy(com.telemedclinic.pharmacy.internal.dto.PharmacyRegisterRequest request) {
        Pharmacy pharmacy = new Pharmacy(
                request.getName(),
                request.getAddress(),
                request.getPhoneNumber(),
                request.getLegalDocumentNumber(),
                request.getLatitude(),
                request.getLongitude()
        );
        return pharmacyRepository.save(pharmacy);
    }

    @Override
    public void registerPharmacist(Long pharmacyId, CreatePharmacistRequestDTO dto) {
        CreatePharmacistRequest request = new CreatePharmacistRequest(
                dto.name(),
                dto.email(),
                dto.phoneNumber(),
                dto.licenseNumber(),
                pharmacyId
        );
        PharmacistProvisioningResult result = pharmacistProvisioningService.provisionPharmacist(request);
        if (result.getPharmacist() != null) {
            result.getPharmacist().setShiftSchedule(dto.shiftSchedule());
            userRepository.save(result.getPharmacist());
        }
    }

    @Override
    public void requestWithdrawal(Long pharmacyId, WithdrawalRequestDTO dto) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new IllegalArgumentException("Pharmacy not found"));
        
        if (pharmacy.getCurrentBalance() < dto.amount()) {
            throw new IllegalStateException("Insufficient balance.");
        }
        
        // TODO: Integrate with Midtrans IRIS
        pharmacy.setCurrentBalance(pharmacy.getCurrentBalance() - dto.amount());
        pharmacyRepository.save(pharmacy);
    }

    @Override
    public long getTodayCompletedTransactionsCount(Long pharmacyId) {
        LocalDate today = LocalDate.now();
        return orderRepository.findByPharmacyIdOrderByCreatedAtDesc(pharmacyId).stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().toLocalDate().equals(today))
                .filter(o -> com.telemedclinic.order.entity.OrderStatus.COMPLETED.equals(o.getStatus()) || com.telemedclinic.order.entity.OrderStatus.SHIPPED.equals(o.getStatus()) || com.telemedclinic.order.entity.OrderStatus.DELIVERED.equals(o.getStatus()))
                .count();
    }

    @Override
    public double getTodayEarnings(Long pharmacyId) {
        LocalDate today = LocalDate.now();
        return orderRepository.findByPharmacyIdOrderByCreatedAtDesc(pharmacyId).stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().toLocalDate().equals(today))
                .filter(o -> com.telemedclinic.order.entity.OrderStatus.COMPLETED.equals(o.getStatus()))
                .mapToDouble(Order::getTotalAmount)
                .sum();
    }

    @Override
    public void updatePharmacist(Long pharmacistId, com.telemedclinic.pharmacy.internal.dto.UpdatePharmacistRequestDTO dto) {
        userRepository.findById(pharmacistId).ifPresent(user -> {
            if (user instanceof com.telemedclinic.pharmacy.internal.entity.Pharmacist pharmacist) {
                pharmacist.setName(dto.name());
                pharmacist.setPhoneNumber(dto.phoneNumber());
                pharmacist.setLicenseNumber(dto.licenseNumber());
                pharmacist.setShiftSchedule(dto.shiftSchedule());
                userRepository.save(pharmacist);
            }
        });
    }

    @Override
    public java.util.List<Order> getRecentOrders(Long pharmacyId) {
        return orderRepository.findByPharmacyIdOrderByCreatedAtDesc(pharmacyId);
    }
}

