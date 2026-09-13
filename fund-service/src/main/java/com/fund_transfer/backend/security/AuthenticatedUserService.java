package com.fund_transfer.backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserService {

    private final String testCif;

    public AuthenticatedUserService(
            @Value("${app.test.cif:TEST-CIF}") String testCif) {

        this.testCif = testCif;
    }

    public AuthenticatedUser getCurrentUser(
            Authentication authentication) {

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof Jwt jwt)) {

            throw new IllegalStateException(
                    "Authenticated user not found"
            );
        }

        String keycloakUserId = jwt.getSubject();

        if (keycloakUserId == null ||
                keycloakUserId.isBlank()) {

            throw new IllegalStateException(
                    "Keycloak user ID not found in access token"
            );
        }

        return new AuthenticatedUser(
                keycloakUserId,
                testCif
        );
    }
}