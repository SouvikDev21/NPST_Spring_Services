package com.fund_transfer.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {

    /**
     * Base URL for the Keycloak server instance.
     */
    private String url = "http://localhost:8080";

    /**
     * Realm name. NPST BCB default is bharat-banking.
     */
    private String realm = "bharat-banking";

    /**
     * Full JWT token issuer URI: ${keycloak.url}/realms/${keycloak.realm}
     */
    private String issuerUri = "http://localhost:8080/realms/bharat-banking";

    /**
     * Primary client identifier used by the service.
     */
    private String clientId = "admin-web";

    /**
     * Client secret (if client is configured as confidential).
     */
    private String clientSecret;

    /**
     * Preferred JWT claim name for extracting customer CIF.
     */
    private String claimCifKey = "cif";

    /**
     * Configured Keycloak client IDs for distinct audiences.
     */
    private Clients clients = new Clients();

    @Data
    public static class Clients {
        /**
         * Keycloak client for bank staff / admin web portal.
         */
        private String adminWeb = "admin-web";

        /**
         * Keycloak client for retail / corporate customer mobile application.
         */
        private String mobileApp = "mobile-app";
    }
}
