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

    private String balanceInquiryPath ;
    private String debitPath = "/";
    private String reversalPath = "/accounts/reverse";

    private int connectTimeoutMs = 3000;
    private int readTimeoutMs = 8000;

    private String apiKeyHeaderName = "X-API-Key";
    private String apiKey;


    private String oauthTokenUrl;
    private String oauthClientId;
    private String oauthClientSecret;


}
