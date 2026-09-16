package com.fund_transfer.backend.config;

import com.fund_transfer.backend.security.KeycloakRoleConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Keycloak-backed resource-server security config.
 *
 * IMPORTANT — what this does and does NOT do:
 * - It REQUIRES every /api/** request to carry a valid Keycloak-issued
 *   Bearer access token (signature + issuer + expiry all verified by
 *   Spring's standard JwtDecoder, wired from
 *   spring.security.oauth2.resourceserver.jwt.issuer-uri in
 *   application.properties). An invalid/missing/expired token -> 401
 *   before the request ever reaches a controller.
 * - It maps Keycloak realm roles onto GrantedAuthority so PermissionService
 *   and @PreAuthorize("@permissionService.hasPermission(...)") work.
 * - It does NOT use the token to resolve which CIF a request acts on.
 *   Per product decision, CIF is supplied explicitly by the frontend on
 *   every beneficiary-management request (see BeneficiaryController's
 *   CIF_HEADER). Authentication (who is calling, via Keycloak) and
 *   authorization-by-CIF (whose data they're touching) are intentionally
 *   two separate, currently-uncorrelated checks here — see the security
 *   note in BeneficiaryController for the IDOR implication of that.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final ObjectProvider<MockAuthFilter> mockAuthFilterProvider;

    public SecurityConfig(ObjectProvider<MockAuthFilter> mockAuthFilterProvider) {
        this.mockAuthFilterProvider = mockAuthFilterProvider;
    }

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

        // LOCAL/DEV ONLY: if app.security.mock-auth-enabled=true, MockAuthFilter exists
        // as a bean and gets inserted ahead of the real bearer-token filter, so it
        // supplies a fake-but-valid Authentication before Spring even looks for a real
        // token. When the property is false/unset, this bean doesn't exist and nothing
        // changes here — real Keycloak validation runs exactly as before.
        MockAuthFilter mockAuthFilter = mockAuthFilterProvider.getIfAvailable();
        if (mockAuthFilter != null) {
            http.addFilterBefore(mockAuthFilter, BearerTokenAuthenticationFilter.class);
        }

        return http.build();
    }

    /**
     * Maps Keycloak REALM roles (the token's "realm_access.roles" claim) onto
     * Spring Security GrantedAuthority, using the RAW role name with NO
     * "ROLE_" prefix (e.g. "RETAIL_CUSTOMER") — matches the convention
     * PermissionService already documents and relies on.
     *
     * NOTE: reads realm roles only. If your Keycloak client is instead
     * configured to issue CLIENT roles (resource_access.<client-id>.roles),
     * change the claim path below to match your realm/client's token mapper.
     */
    @Bean
    public JwtAuthenticationConverter keycloakJwtAuthenticationConverter() {
        Converter<Jwt, Collection<GrantedAuthority>> realmRolesConverter = jwt -> {
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess == null || realmAccess.get("roles") == null) {
                return List.of();
            }
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");
            return roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        };

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(realmRolesConverter);
        return converter;
    }

//    {
//        "timestamp": "2026-09-14T17:30:20+05:30",
//            "status": 403,
//            "error": "ACCESS_DENIED",
//            "message": "You do not have permission to perform this action",
//            "path": "/api/v1/beneficiaries"
//    }
    private AccessDeniedHandler jsonAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(org.springframework.http.HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            // Written by hand (no ObjectMapper dependency here) since this handler
            // runs inside the security filter chain, ahead of the DispatcherServlet/
            // Jackson message converters — @RestControllerAdvice can't reach it.
            String json = "{"
                    + "\"timestamp\":\"" + OffsetDateTime.now() + "\","
                    + "\"status\":403,"
                    + "\"error\":\"ACCESS_DENIED\","
                    + "\"message\":\"You do not have permission to perform this action\","
                    + "\"path\":\"" + request.getRequestURI() + "\""
                    + "}";
            try {
                response.getWriter().write(json);
            } catch (IOException ignored) {
                // response stream already committed/closed — nothing more we can do here
            }
        };
    }
}