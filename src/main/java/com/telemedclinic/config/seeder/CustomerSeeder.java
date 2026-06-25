package com.telemedclinic.config.seeder;

import java.time.LocalDate;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.telemedclinic.user.entity.Customer;
import com.telemedclinic.user.entity.Gender;
import com.telemedclinic.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerSeeder {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void seed() {
        // Customer Default
        if (!userRepository.existsByEmail("customer@gmail.com")) {
            Customer customer = new Customer(
                    "Pengguna Default",
                    "customer@gmail.com",
                    passwordEncoder.encode("customer123"),
                    "081211111111",
                    "Jl. Sehat No. 1",
                    Gender.MALE,
                    LocalDate.of(1990, 1, 1),
                    170.0,
                    70.0
            );
            userRepository.save(customer);
        }

        // Customer Arby
        if (!userRepository.existsByEmail("arby@gmail.com")) {
            Customer arby = new Customer(
                    "Arby",
                    "arby@gmail.com",
                    passwordEncoder.encode("arby123"),
                    "081222222222",
                    "Jl. Alun-Alun Purwokerto",
                    Gender.MALE,
                    LocalDate.of(1995, 5, 5),
                    175.0,
                    65.0
            );
            arby.setLatitude(-7.424419);
            arby.setLongitude(109.230279);
            userRepository.save(arby);
        }

        // Customer Wahyu
        if (!userRepository.existsByEmail("wahyu@gmail.com")) {
            Customer wahyu = new Customer(
                    "Wahyu",
                    "wahyu@gmail.com",
                    passwordEncoder.encode("wahyu123"),
                    "081233333333",
                    "Jl. Baturraden KM 5",
                    Gender.MALE,
                    LocalDate.of(1998, 8, 8),
                    165.0,
                    60.0
            );
            wahyu.setLatitude(-7.406982);
            wahyu.setLongitude(109.250550);
            userRepository.save(wahyu);
        }
        log.info("Customers berhasil disesuaikan");
    }
}
