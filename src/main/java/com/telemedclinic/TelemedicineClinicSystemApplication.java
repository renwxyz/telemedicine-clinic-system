package com.telemedclinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TelemedicineClinicSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(TelemedicineClinicSystemApplication.class, args);
	}

}
