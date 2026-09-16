package com.fund_transfer.backend.Otp;

import feign.Request;
import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

/**
 * Feign-specific wiring for OtpFeignClient — the equivalent of what
 * OtpClientConfig used to build by hand for the RestClient version
 * (connect/read timeout + the optional static API key header).
 *
 * NOT annotated @Configuration on purpose: this is passed via
 * @FeignClient(configuration = ...) on OtpFeignClient, so it must stay out of
 * normal component scanning (a plain @Configuration class here would apply
 * these beans globally to every Feign client, not just this one).
 */
@RequiredArgsConstructor
public class OtpFeignClientConfig {

    private final OtpProperties otpProperties;

    @Bean
    public Request.Options otpFeignRequestOptions() {
        return new Request.Options(
                otpProperties.getConnectTimeoutMs(), TimeUnit.MILLISECONDS,
                otpProperties.getReadTimeoutMs(), TimeUnit.MILLISECONDS,
                true
        );
    }

    @Bean
    public RequestInterceptor otpFeignApiKeyInterceptor() {
        return requestTemplate -> {
            if (otpProperties.getApiKey() != null && !otpProperties.getApiKey().isBlank()) {
                requestTemplate.header(otpProperties.getApiKeyHeaderName(), otpProperties.getApiKey());
            }
        };
    }
}
