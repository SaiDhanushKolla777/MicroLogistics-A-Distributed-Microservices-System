package com.micrologistics.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for API Gateway routes.
 * Defines routing rules to direct requests to the appropriate microservices.
 */
@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Item Registration Service Routes
                .route("item-registration", r -> r
                        .path("/api/items/**")
                        .filters(f -> f
                                .rewritePath("/api/(?<segment>.*)", "/${segment}")
                                .retry(config -> config.setRetries(3)
                                        .setStatuses(500, 503)
                                        .setBackoff(50, 500, 2, true)))
                        .uri("lb://ITEM-REGISTRATION"))
                
                // Routing Service Routes
                .route("routing-service", r -> r
                        .path("/api/routes/**")
                        .filters(f -> f
                                .rewritePath("/api/(?<segment>.*)", "/${segment}")
                                .retry(config -> config.setRetries(3)
                                        .setStatuses(500, 503)
                                        .setBackoff(50, 500, 2, true)))
                        .uri("lb://ROUTING-SERVICE"))
                
                // Container Management Service Routes
                .route("container-management", r -> r
                        .path("/api/containers/**")
                        .filters(f -> f
                                .rewritePath("/api/(?<segment>.*)", "/${segment}")
                                .retry(config -> config.setRetries(3)
                                        .setStatuses(500, 503)
                                        .setBackoff(50, 500, 2, true)))
                        .uri("lb://CONTAINER-MANAGEMENT"))
                
                // Metrics Service Routes
                .route("metrics-service", r -> r
                        .path("/api/metrics/**")
                        .filters(f -> f
                                .rewritePath("/api/(?<segment>.*)", "/${segment}")
                                .retry(config -> config.setRetries(2)
                                        .setStatuses(500, 503)
                                        .setBackoff(50, 500, 2, true)))
                        .uri("lb://METRICS-SERVICE"))
                
                // Dashboard Service Routes
                .route("dashboard-service", r -> r
                        .path("/api/dashboard/**", "/dashboard/**")
                        .filters(f -> f
                                .rewritePath("/api/(?<segment>.*)", "/${segment}")
                                .retry(config -> config.setRetries(2)
                                        .setStatuses(500, 503)
                                        .setBackoff(50, 500, 2, true)))
                        .uri("lb://DASHBOARD-SERVICE"))
                
                .build();
    }
}
