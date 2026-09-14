package com.fund_transfer.backend.config;

import com.fund_transfer.backend.security.KeycloakRoleConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http) throws Exception {

        JwtAuthenticationConverter jwtAuthenticationConverter =
                new JwtAuthenticationConverter();

        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
                new KeycloakRoleConverter()
        );

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )

                .exceptionHandling(exception -> exception

                        .authenticationEntryPoint(
                                (request, response, ex) -> {
                                    response.setStatus(401);
                                    response.setContentType("application/json");
                                    response.getWriter().write(
                                            "{\"error\":\"UNAUTHORIZED\"}"
                                    );
                                }
                        )

                        .accessDeniedHandler(
                                (request, response, ex) -> {
                                    response.setStatus(403);
                                    response.setContentType("application/json");
                                    response.getWriter().write(
                                            "{\"error\":\"FORBIDDEN\"}"
                                    );
                                }
                        )
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                );

        return http.build();
    }
}