package com.bank.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Entry point for the API Gateway.
 *
 * This service does NOT contain any business logic. It only:
 *  - Routes incoming requests to the correct downstream microservice
 *  - Discovers Spring Boot services via Eureka (lb://SERVICE-NAME)
 *  - Routes to non-Eureka services (like the NestJS Auth Service) via direct URLs
 *  - Validates JWT tokens issued by Keycloak before allowing requests through
 *
 * It registers itself with Eureka under the application name "API-GATEWAY"
 * (configured in application.yml via spring.application.name).
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}
