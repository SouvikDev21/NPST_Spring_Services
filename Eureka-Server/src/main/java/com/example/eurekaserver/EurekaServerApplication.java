package com.example.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Server Application
 *
 * This acts as the Service Registry for all microservices in the bank app
 * ecosystem (e.g., account-service, beneficiary-service, otp-service,
 * transaction-service, notification-service).
 *
 * Each microservice registers itself here on startup, and other services
 * discover each other's locations through this registry instead of using
 * hardcoded URLs — enabling load balancing, failover, and dynamic scaling.
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }

}
