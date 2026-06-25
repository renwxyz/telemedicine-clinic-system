package com.telemedclinic;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.telemedclinic.user.repository.AdminRepository;
import com.telemedclinic.cart.repository.CartItemRepository;
import com.telemedclinic.consultation.repository.ConsultationRepository;
import com.telemedclinic.pharmacy.internal.repository.InventoryItemRepository;
import com.telemedclinic.order.repository.OrderRepository;
import com.telemedclinic.pharmacy.internal.repository.PharmacyRepository;
import com.telemedclinic.prescription.repository.PrescriptionItemRepository;
import com.telemedclinic.prescription.repository.PrescriptionRepository;
import com.telemedclinic.user.repository.DoctorRepository;
import com.telemedclinic.user.repository.UserRepository;

@SpringBootTest(properties = {
		"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
@ActiveProfiles("test")
class TelemedicineClinicSystemApplicationTests {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private DoctorRepository doctorRepository;

    @MockBean
    private AdminRepository adminRepository;

    @MockBean
    private ConsultationRepository consultationRepository;

    @MockBean
    private CartItemRepository cartItemRepository;

    @MockBean
    private InventoryItemRepository inventoryItemRepository;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private PrescriptionRepository prescriptionRepository;

    @MockBean
    private PrescriptionItemRepository prescriptionItemRepository;

    @MockBean
    private PharmacyRepository pharmacyRepository;

    @Test
    void contextLoads() {
    }

}
