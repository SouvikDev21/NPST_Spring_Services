package com.fund_transfer.backend.security;

public record AuthenticatedUser(
        String keycloakUserId,
        String cif
) {
}