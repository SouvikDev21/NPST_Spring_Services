package com.common.keycloak;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {

    private String url;
    private String authServerUrl;
    private String realm;
    private String issuerUri;
    private String jwkSetUri;
    private String clientId;
    private String clientSecret;
    private String claimCifKey = "cif";
    private String tokenValidation = "offline";
    private String adminUsername;
    private String adminPassword;
    private Clients clients = new Clients();

    public String getIssuerUri() {
        if (issuerUri != null && !issuerUri.isBlank()) {
            return issuerUri;
        }
        String server = authServerUrl != null && !authServerUrl.isBlank() ? authServerUrl : url;
        if (server != null && !server.isBlank() && realm != null && !realm.isBlank()) {
            return (server.endsWith("/") ? server.substring(0, server.length() - 1) : server) + "/realms/" + realm;
        }
        return null;
    }

    @Data
    public static class Clients {
        private String adminWeb;
        private String mobileApp;
    }
}
