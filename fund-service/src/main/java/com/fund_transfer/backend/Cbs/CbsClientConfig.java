package com.fund_transfer.backend.Cbs;


import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class CbsClientConfig {

    private final CbsProperties cbsProperties;

    @Bean
    public RestClient CbsRestClient() {

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(cbsProperties.getConnectTimeoutMs()))
                .setResponseTimeout(Timeout.ofMilliseconds(cbsProperties.getReadTimeoutMs()))
                .build();

        var httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(cbsProperties.getBaseUrl())
                .requestFactory(requestFactory);

        if (cbsProperties.getApiKey() != null && !cbsProperties.getApiKey().isBlank()) {
            builder.defaultHeader(cbsProperties.getApiKeyHeaderName(), cbsProperties.getApiKey());
        }

        return builder.build();
    }
}
