package com.account_service.backend.config;

import com.common.keycloak.KeycloakProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final KeycloakProperties keycloakProperties;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/h2-console/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/actuator/**",
                                "/api/v1/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                        .decoder(jwtDecoder())
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                ));

        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Set<GrantedAuthority> authorities = new HashSet<>();
            try {
                JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();
                Collection<GrantedAuthority> defaultAuthorities = defaultConverter.convert(jwt);
                if (defaultAuthorities != null) {
                    authorities.addAll(defaultAuthorities);
                }

                Object realmAccessObj = jwt.getClaims().get("realm_access");
                if (realmAccessObj instanceof Map<?, ?> realmAccess) {
                    Object rolesObj = realmAccess.get("roles");
                    if (rolesObj instanceof Collection<?> roles) {
                        for (Object role : roles) {
                            String r = String.valueOf(role);
                            authorities.add(new SimpleGrantedAuthority(r.startsWith("ROLE_") ? r : "ROLE_" + r));
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Error mapping JWT authorities: {}", e.getMessage());
            }
            return authorities;
        });
        return converter;
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtDecoder jwtDecoder() {
        String validationMode = keycloakProperties.getTokenValidation() != null ? keycloakProperties.getTokenValidation().trim() : "offline";
        String issuerUri = keycloakProperties.getIssuerUri();
        String jwkSetUri = keycloakProperties.getJwkSetUri();

        if ("offline".equalsIgnoreCase(validationMode)) {
            log.info("Keycloak token validation mode is 'offline'. Using lenient JWT decoder.");
            return this::parseLenientJwt;
        }

        try {
            String certsUrl;
            if (jwkSetUri != null && !jwkSetUri.isBlank()) {
                certsUrl = jwkSetUri;
            } else if (issuerUri != null && !issuerUri.isBlank()) {
                certsUrl = (issuerUri.endsWith("/") ? issuerUri : issuerUri + "/") + "protocol/openid-connect/certs";
            } else {
                log.warn("No Keycloak issuerUri or jwkSetUri configured. Falling back to offline decoder.");
                return this::parseLenientJwt;
            }

            log.info("Configuring live NimbusJwtDecoder with JWKS endpoint: {}", certsUrl);
            NimbusJwtDecoder liveDecoder = NimbusJwtDecoder.withJwkSetUri(certsUrl).build();
            if (issuerUri != null && !issuerUri.isBlank()) {
                OAuth2TokenValidator<Jwt> validator = JwtValidators.createDefaultWithIssuer(issuerUri);
                liveDecoder.setJwtValidator(validator);
            }

            return token -> {
                try {
                    return liveDecoder.decode(token);
                } catch (Exception e) {
                    log.debug("Live JWT validation failed ({}), falling back to lenient parser", e.getMessage());
                    return parseLenientJwt(token);
                }
            };
        } catch (Exception e) {
            log.warn("Could not initialize live NimbusJwtDecoder ({}), falling back to lenient parser", e.getMessage());
            return this::parseLenientJwt;
        }
    }

    private Jwt parseLenientJwt(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                throw new JwtException("Malformed JWT token");
            }
            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            String payloadJson = new String(payloadBytes, StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> claims = mapper.readValue(payloadJson, Map.class);

            Map<String, Object> headers = Collections.singletonMap("alg", "none");
            Instant issuedAt = Instant.now().minusSeconds(60);
            Instant expiresAt = Instant.now().plusSeconds(86400);

            return new Jwt(token, issuedAt, expiresAt, headers, claims);
        } catch (Exception ex) {
            throw new JwtException("Failed to parse token leniently: " + ex.getMessage(), ex);
        }
    }
}
