package com.common.keycloak;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {

    private String url;
    private String realm;
    private String issuerUri;
    private String clientId;
    private String clientSecret;
    private String claimCifKey;
    private Clients clients = new Clients();

    @Data
    public static class Clients {
        private String adminWeb;
        private String mobileApp;
    }
}
