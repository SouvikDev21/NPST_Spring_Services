package com.bank.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Reactive (WebFlux) security configuration for the API Gateway.
 *
 * This is NOT the servlet-based SecurityFilterChain / HttpSecurity used in
 * traditional Spring MVC apps. Because Spring Cloud Gateway runs on WebFlux,
 * we must use ServerHttpSecurity and SecurityWebFilterChain instead.
 *
 * Responsibilities:
 *  - Allow public, unauthenticated access to /api/auth/** (login/registration
 *    endpoints served by the NestJS Auth Service) and /actuator/** (dev only).
 *  - Require a valid JWT (issued by Keycloak) on every other route.
 *  - Validate JWTs using Keycloak's issuer URI, which Spring Security uses to
 *    automatically discover Keycloak's public signing keys (JWK Set) via
 *    OpenID Connect discovery.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                // Gateways are typically stateless APIs consumed by other services/clients,
                // so CSRF protection (which is cookie/session based) is not needed here.
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .authorizeExchange(exchanges -> exchanges
                        // Public: users must be able to reach login/registration without a token
                        .pathMatchers("/api/auth/**").permitAll()
                        // Public: actuator endpoints open for local development/monitoring.
                        // TODO: lock this down (e.g. permit only /actuator/health) before production.
                        .pathMatchers("/actuator/**").permitAll()
                        // Everything else requires a valid, authenticated JWT
                        .anyExchange().authenticated()
                )

                // Enable OAuth2 Resource Server support, using JWT validation.
                // The issuer URI configured in application.yml
                // (spring.security.oauth2.resourceserver.jwt.issuer-uri) tells
                // Spring Security where to fetch Keycloak's public keys from.
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {
                            // Default JWT decoder built from the issuer-uri in application.yml.
                            // No custom converter is configured here; add a
                            // ReactiveJwtAuthenticationConverter if you need to map
                            // Keycloak realm/client roles into Spring Security authorities.
                        })
                );

        return http.build();
    }

}
