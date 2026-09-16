package com.fund_transfer.backend.Otp;

import com.fund_transfer.backend.dto.Request.OtpSendRequest;
import com.fund_transfer.backend.dto.Request.OtpVerifyRequest;
import com.fund_transfer.backend.dto.Response.OtpSendResponse;
import com.fund_transfer.backend.dto.Response.OtpVerifyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * OpenFeign declaration of the external OTP/auth service.
 *
 * baseUrl is resolved from otp.base-url at startup (see OtpProperties /
 * application.properties). Paths below mirror the defaults in
 * OtpProperties#sendPath / #verifyPath — if those properties are ever
 * overridden away from the defaults, update the paths here too, since Feign
 * mappings (unlike the old RestClient call sites) are fixed at compile time.
 *
 * All cross-cutting concerns (connect/read timeout, the optional API key
 * header) live in OtpFeignClientConfig, not here.
 */
@FeignClient(
        name = "otpService",
        url = "${otp.base-url}",
        configuration = OtpFeignClientConfig.class
)
public interface OtpFeignClient {

    @PostMapping("/api/v1/app/auth/otp/send")
    OtpSendResponse sendOtp(@RequestBody OtpSendRequest request);

    @PostMapping("/api/v1/app/auth/otp/verify")
    OtpVerifyResponse verifyOtp(@RequestBody OtpVerifyRequest request);
}
