package com.fund_transfer.backend.adapter.keycloak;

import com.fund_transfer.backend.common.constants.KeycloakConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakUserContext {
    private String userId; // Keycloak sub UUID
    private String cif;
    private String username;
    private String email;
    private Set<String> roles;
    private boolean authenticated;

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    public boolean isBankStaff() {
        return hasRole(KeycloakConstants.ROLE_BANK_SUPER_ADMIN)
                || hasRole(KeycloakConstants.ROLE_BANK_ADMIN)
                || hasRole(KeycloakConstants.ROLE_BANK_MAKER)
                || hasRole(KeycloakConstants.ROLE_BANK_CHECKER);
    }

    public boolean isCorporate() {
        return hasRole(KeycloakConstants.ROLE_CORPORATE_IT_ADMIN)
                || hasRole(KeycloakConstants.ROLE_CORPORATE_MAKER)
                || hasRole(KeycloakConstants.ROLE_CORPORATE_CHECKER)
                || hasRole(KeycloakConstants.ROLE_CORPORATE_VIEWER);
    }

    public boolean isRetailCustomer() {
        return hasRole(KeycloakConstants.ROLE_RETAIL_CUSTOMER);
    }

    public static KeycloakUserContext anonymous() {
        return KeycloakUserContext.builder()
                .username(KeycloakConstants.ANONYMOUS_USER)
                .roles(Collections.emptySet())
                .authenticated(false)
                .build();
    }
}
