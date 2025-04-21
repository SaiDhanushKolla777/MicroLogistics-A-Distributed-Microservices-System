package com.micrologistics.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Logging filter for the API Gateway.
 * Logs details of incoming requests and outgoing responses.
 */
@Component
@Slf4j
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();
        
        log.info("Request: {} {}", method, path);
        
        // Record the start time
        long startTime = System.currentTimeMillis();
        
        // Continue the filter chain and log the response status and time taken when complete
        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    long duration = System.currentTimeMillis() - startTime;
                    int statusCode = exchange.getResponse().getStatusCode() != null 
                            ? exchange.getResponse().getStatusCode().value() 
                            : -1;
                    
                    log.info("Response: {} {} - Status: {}, Time: {}ms", 
                            method, path, statusCode, duration);
                    
                    // Log additional metrics for slow requests
                    if (duration > 1000) {
                        log.warn("Slow request detected: {} {} - {}ms", method, path, duration);
                    }
                }));
    }

    @Override
    public int getOrder() {
        // Execute this filter before the AuthenticationFilter
        return -200;
    }
}
