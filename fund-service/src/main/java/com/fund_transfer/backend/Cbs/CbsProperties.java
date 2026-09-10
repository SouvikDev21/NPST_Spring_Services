package com.fund_transfer.backend.Cbs;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "cbs")
public class CbsProperties {

    // Base URL of the CBS server, e.g. https://cbs.internal.bank:8443
    private String baseUrl;

    // Endpoint paths — adjust to match CBS's actual API once you have docs.
    private String balanceInquiryPath = "/accounts/{accountNumber}/balance";
    private String debitPath = "/accounts/debit";
    private String reversalPath = "/accounts/reverse";

    // Connection/read timeouts in milliseconds.
    private int connectTimeoutMs = 3000;
    private int readTimeoutMs = 8000;

    // --- Auth: fill in whichever applies once you confirm with CBS docs/team ---

    // Option A: static API key sent as a header (most common for internal CBS gateways)
    private String apiKeyHeaderName = "X-API-Key";
    private String apiKey;

    // Option B: OAuth2 client-credentials — if used, wire a WebClient with an
    // OAuth2AuthorizedClientManager instead of the static header below.
    private String oauthTokenUrl;
    private String oauthClientId;
    private String oauthClientSecret;

    // Option C: mTLS — if used, auth happens at the TLS layer via a configured
    // SSL keystore/truststore on the WebClient's HttpClient, not via headers.
    // No properties needed here beyond keystore path/password, usually set
    // via standard Spring SSL bundle config in application.yml.
}
