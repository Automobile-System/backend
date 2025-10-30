package com.TenX.Automobile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Automobile Enterprise System - Main Application
 * Spring Boot application with enterprise-level security and RBAC
 */
@SpringBootApplication
@EnableScheduling
public class AutomobileApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutomobileApplication.class, args);
	}

}
