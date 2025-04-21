package com.micrologistics.item;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * The Item Registration Service application.
 * This service handles the registration and management of items in the logistics system.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ItemRegistrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItemRegistrationApplication.class, args);
    }
}
