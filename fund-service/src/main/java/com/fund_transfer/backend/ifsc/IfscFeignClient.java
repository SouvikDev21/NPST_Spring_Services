package com.fund_transfer.backend.ifsc;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * OpenFeign declaration of the Razorpay IFSC lookup API:
 * GET https://ifsc.razorpay.com/{IFSC_CODE}
 *
 * Replaces the old RestTemplate call in IfscLookupService. Timeouts
 * (connect=3s, read=5s — same values IfscConfig used to set on the
 * RestTemplate) are configured via application.properties, see
 * spring.cloud.openfeign.client.config.ifscService.* below.
 */
@FeignClient(name = "ifscService", url = "https://ifsc.razorpay.com")
public interface IfscFeignClient {

//    Spring Cloud OpenFeign creates the implementation for you at runtime.
//
//            Conceptually, imagine Spring creates something like this behind the scenes:
//
//    class IfscFeignClientImpl implements IfscFeignClient {
//
//        @Override
//        public IfscDetailsResponse getBranchDetails(String ifscCode) {

    // Build HTTP request
    // GET https://ifsc.razorpay.com/{ifscCode}

    // Send request

    // Receive JSON

    // Convert JSON → IfscDetailsResponse

    // return response
//}





    @GetMapping("/{ifscCode}")
    IfscDetailsResponse getBranchDetails(@PathVariable("ifscCode") String ifscCode);
}
