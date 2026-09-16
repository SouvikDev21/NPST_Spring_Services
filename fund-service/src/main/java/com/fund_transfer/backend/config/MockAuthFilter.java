package com.fund_transfer.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * LOCAL/DEV ONLY. When app.security.mock-auth-enabled=true (see
 * application.properties), this filter fabricates a fake, already-verified
 * JWT authentication for EVERY request, so you can hit protected endpoints
 * from Postman without a real Keycloak token.
 *
 * This is a deliberate, explicit bypass of real authentication — it exists
 * ONLY to unblock local testing before Keycloak is wired up end-to-end.
 *
 * !! MUST be disabled (app.security.mock-auth-enabled=false) in any shared,
 * staging, or production environment !! Leaving this on anywhere reachable
 * by anyone other than you defeats authentication entirely — every request
 * is treated as an authenticated RETAIL_CUSTOMER named "local-dev-user"
 * regardless of who sends it or what Authorization header (if any) they
 * send.
 *
 * Fields you can override via application.properties, to match whatever
 * cif/user you're testing with:
 *   app.security.mock-auth-subject
 *   app.security.mock-auth-roles (comma-separated)
 */
@Component
@ConditionalOnProperty(name = "app.security.mock-auth-enabled", havingValue = "true")
public class MockAuthFilter extends OncePerRequestFilter {

    private final String subject;
    private final List<String> roles;

    public MockAuthFilter(
            @Value("${app.security.mock-auth-subject:local-dev-user}") String subject,
            @Value("${app.security.mock-auth-roles:RETAIL_CUSTOMER}") String rolesCsv) {
        this.subject = subject;
        this.roles = List.of(rolesCsv.split(","));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // Build a fake-but-shaped-correctly Jwt so @AuthenticationPrincipal Jwt jwt
        // and jwt.getSubject() work exactly like they would with a real token.
        Jwt fakeJwt = Jwt.withTokenValue("mock-local-dev-token")
                .header("alg", "none")
                .claim("sub", subject)
                .claim("realm_access", Map.of("roles", roles))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        List<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();

        JwtAuthenticationToken authToken = new JwtAuthenticationToken(fakeJwt, authorities, subject);

        SecurityContextHolder.getContext().setAuthentication(authToken);

        try {
            chain.doFilter(request, response);
        } finally {
            // Don't leak this fake auth beyond the current request.
            SecurityContextHolder.clearContext();
        }
    }
}