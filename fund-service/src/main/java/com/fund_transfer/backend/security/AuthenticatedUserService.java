package com.fund_transfer.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserService {

    public AuthenticatedUser getCurrentUser(
            Authentication authentication) {

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof Jwt jwt)) {

            throw new IllegalStateException(
                    "Authenticated user not found"
            );
        }

        String keycloakUserId = jwt.getSubject();

        /*
         * The exact claim name for CIF depends on your
         * Keycloak/Auth Service token configuration.
         *
         * Change "cif" if your JWT uses another claim.
         */
        String cif = jwt.getClaimAsString("cif");

        if (cif == null || cif.isBlank()) {
            throw new IllegalStateException(
                    "CIF not found in access token"
            );
        }

        return new AuthenticatedUser(
                keycloakUserId,
                cif
        );
    }
}