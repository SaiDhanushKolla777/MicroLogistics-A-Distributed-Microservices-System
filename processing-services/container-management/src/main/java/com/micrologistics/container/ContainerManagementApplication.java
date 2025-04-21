package com.micrologistics.container;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * The Container Management Service application.
 * This service manages containers and optimizes item placement for efficient
 * space utilization and shipping.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ContainerManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContainerManagementApplication.class, args);
    }
}
