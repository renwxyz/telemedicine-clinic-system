package com.telemedclinic.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.telemedclinic.config.seeder.AdminSeeder;
import com.telemedclinic.config.seeder.CustomerSeeder;
import com.telemedclinic.config.seeder.DoctorSeeder;
import com.telemedclinic.config.seeder.MedicineSeeder;
import com.telemedclinic.config.seeder.PharmacistSeeder;
import com.telemedclinic.config.seeder.PharmacyOwnerSeeder;
import com.telemedclinic.config.seeder.PharmacySeeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final AdminSeeder adminSeeder;
    private final CustomerSeeder customerSeeder;
    private final DoctorSeeder doctorSeeder;
    private final PharmacyOwnerSeeder pharmacyOwnerSeeder;
    private final PharmacySeeder pharmacySeeder;
    private final PharmacistSeeder pharmacistSeeder;
    private final MedicineSeeder medicineSeeder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Starting Data Seeding...");
        adminSeeder.seed();
        customerSeeder.seed();
        doctorSeeder.seed();
        pharmacyOwnerSeeder.seed();
        pharmacySeeder.seed();
        pharmacistSeeder.seed();
        medicineSeeder.seed();
        log.info("Data Seeding Completed!");
    }
}
