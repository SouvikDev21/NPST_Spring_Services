package com.bank.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Reactive (WebFlux) security configuration for the API Gateway.
 *
 * !! DEV MODE — ALL ROUTES CURRENTLY PUBLIC !!
 * Authentication is fully disabled below (.anyExchange().permitAll()).
 * This is only safe for local development. Before any shared/staging/
 * production deployment, revert to requiring JWT auth on all routes
 * except /api/auth/** and /actuator/** — see commented-out block below.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .authorizeExchange(exchanges -> exchanges
                                // DEV ONLY: everything open, no JWT required anywhere.
                                .anyExchange().permitAll()

                        // --- Original (production-intended) rules, disabled for now ---
                        // .pathMatchers("/api/auth/**").permitAll()
                        // .pathMatchers("/actuator/**").permitAll()
                        // .anyExchange().authenticated()
                )

                // OAuth2 resource server support stays configured so JWTs are still
                // validated and parsed IF a client sends one — it's just no longer
                // required. Safe to leave in place even in dev mode.
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {
                        })
                );

        return http.build();
    }

}