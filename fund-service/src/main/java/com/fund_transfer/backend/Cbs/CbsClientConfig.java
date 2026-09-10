//package com.fund_transfer.backend.Cbs;
//
//import io.netty.channel.ChannelOption;
//import io.netty.handler.timeout.ReadTimeoutHandler;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.client.reactive.ReactorClientHttpConnector;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.netty.http.client.HttpClient;
//
//import java.util.concurrent.TimeUnit;
//
//@Configuration
//@RequiredArgsConstructor
//public class CbsClientConfig {
//
//    private final CbsProperties cbsProperties;
//
//    @Bean
//    public WebClient cbsWebClient() {
//        HttpClient httpClient = HttpClient.create()
//                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, cbsProperties.getConnectTimeoutMs())
//                .doOnConnected(conn -> conn.addHandlerLast(
//                        new ReadTimeoutHandler(cbsProperties.getReadTimeoutMs(), TimeUnit.MILLISECONDS)));
//
//        WebClient.Builder builder = WebClient.builder()
//                .baseUrl(cbsProperties.getBaseUrl())
//                .clientConnector(new ReactorClientHttpConnector(httpClient));
//
//        // --- Auth wiring: uncomment whichever matches once confirmed ---
//
//        // Option A: static API key header
//        if (cbsProperties.getApiKey() != null && !cbsProperties.getApiKey().isBlank()) {
//            builder.defaultHeader(cbsProperties.getApiKeyHeaderName(), cbsProperties.getApiKey());
//        }
//
//        // Option B: OAuth2 client-credentials — instead of the header above,
//        // you'd typically add a ServerOAuth2AuthorizedClientExchangeFilterFunction
//        // as a filter() on this builder, backed by a ClientRegistration built
//        // from cbsProperties.oauthTokenUrl/clientId/clientSecret. Left out
//        // until you confirm this is the auth mechanism, since it needs the
//        // spring-security-oauth2-client dependency.
//
//        // Option C: mTLS — configured at the HttpClient/SslContext level, not
//        // here. Ask CBS team whether client certs are required; if so, this
//        // whole method needs an SslContextBuilder with your keystore/truststore.
//
//        return builder.build();
//    }
//}