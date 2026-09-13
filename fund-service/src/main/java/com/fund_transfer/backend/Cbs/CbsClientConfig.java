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
        // RequestConfig carries the connect timeout; "response timeout" here
        // is the Apache HttpClient 5 equivalent of what we called "read timeout"
        // when this was WebClient — how long to wait for CBS to respond once
        // the request has been sent.
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

        // --- Auth wiring: uncomment/adjust whichever matches once confirmed ---

        // Option A: static API key header — current default assumption
        if (cbsProperties.getApiKey() != null && !cbsProperties.getApiKey().isBlank()) {
            builder.defaultHeader(cbsProperties.getApiKeyHeaderName(), cbsProperties.getApiKey());
        }

        // Option B: OAuth2 client-credentials — RestClient doesn't have a
        // built-in OAuth2 filter like WebClient's ServerOAuth2AuthorizedClientExchangeFilterFunction.
        // Simplest approach: fetch/cache a token yourself (e.g. a small
        // CbsTokenProvider bean using RestClient against cbs.oauth-token-url),
        // then add it as a request interceptor here:
        //   builder.requestInterceptor((request, body, execution) -> {
        //       request.getHeaders().setBearerAuth(tokenProvider.getToken());
        //       return execution.execute(request, body);
        //   });

        // Option C: mTLS — configure the underlying Apache HttpClient's
        // SSLContext with your keystore/truststore instead of a header, e.g.
        // HttpClients.custom().setConnectionManager(
        //     PoolingHttpClientConnectionManagerBuilder.create()
        //         .setSSLSocketFactory(SSLConnectionSocketFactoryBuilder.create()
        //             .setSslContext(yourSslContext).build())
        //         .build())

        return builder.build();
    }
}
