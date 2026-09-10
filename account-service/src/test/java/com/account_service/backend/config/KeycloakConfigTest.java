package com.account_service.backend.config;

import com.common.keycloak.KeycloakAdapter;
import com.common.keycloak.KeycloakConstants;
import com.common.keycloak.KeycloakProperties;
import com.common.keycloak.KeycloakUserContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class KeycloakConfigTest {

    @Autowired
    private KeycloakProperties keycloakProperties;

    @Autowired
    private Environment environment;

    @Autowired
    private KeycloakAdapter keycloakAdapter;

    @Test
    void testKeycloakPropertiesBinding() {
        assertThat(keycloakProperties).isNotNull();
        assertThat(keycloakProperties.getRealm()).isEqualTo("bharat-banking");
        assertThat(keycloakProperties.getIssuerUri()).isEqualTo("http://localhost:8080/realms/bharat-banking");
        assertThat(keycloakProperties.getClients().getAdminWeb()).isEqualTo("admin-web");
        assertThat(keycloakProperties.getClients().getMobileApp()).isEqualTo("mobile-app");
    }

    @Test
    void testDotenvLoadedInEnvironment() {
        assertThat(environment.getProperty("KEYCLOAK_REALM")).isEqualTo("bharat-banking");
        assertThat(environment.getProperty("KEYCLOAK_CLIENT_ADMIN_WEB")).isEqualTo("admin-web");
        assertThat(environment.getProperty("KEYCLOAK_CLIENT_MOBILE_APP")).isEqualTo("mobile-app");
    }

    @Test
    void testKeycloakUserContextRoleChecks() {
        KeycloakUserContext staffContext = KeycloakUserContext.builder()
                .userId("uuid-staff-1")
                .username("bank.maker.01")
                .roles(Set.of(KeycloakConstants.ROLE_BANK_MAKER))
                .authenticated(true)
                .build();

        assertThat(staffContext.isBankStaff()).isTrue();
        assertThat(staffContext.isCorporate()).isFalse();
        assertThat(staffContext.hasRole(KeycloakConstants.ROLE_BANK_MAKER)).isTrue();

        KeycloakUserContext corporateContext = KeycloakUserContext.builder()
                .userId("uuid-corp-1")
                .username("corp-maker-01")
                .cif("CIF100001")
                .roles(Set.of(KeycloakConstants.ROLE_CORPORATE_MAKER))
                .authenticated(true)
                .build();

        assertThat(corporateContext.isCorporate()).isTrue();
        assertThat(corporateContext.isBankStaff()).isFalse();
    }
}
