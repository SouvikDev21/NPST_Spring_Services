package com.fund_transfer.backend.ifsc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.regex.Pattern;

@Service
public class IfscLookupService {

    private static final Logger log = LoggerFactory.getLogger(IfscLookupService.class);

    private static final Pattern IFSC_PATTERN = Pattern.compile("^[A-Z]{4}0[A-Z0-9]{6}$");
    private static final String RAZORPAY_IFSC_BASE_URL = "https://ifsc.razorpay.com/";

    private final RestTemplate restTemplate;

    public IfscLookupService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Cacheable(value = "ifscDetails", key = "#ifscCode")
    public IfscDetailsResponse lookup(String ifscCode) {
        String normalized = normalizeAndValidate(ifscCode);

        try {
            IfscDetailsResponse response = restTemplate.getForObject(
                    RAZORPAY_IFSC_BASE_URL + normalized,
                    IfscDetailsResponse.class
            );

            if (response == null || response.getIfsc() == null) {
                throw new IfscNotFoundException(normalized);
            }

            return response;

        } catch (HttpClientErrorException.NotFound e) {
            throw new IfscNotFoundException(normalized);

        } catch (RestClientException e) {
            log.error("IFSC upstream lookup failed for {}: {}", normalized, e.getMessage());
            throw new IfscLookupUnavailableException(
                    "IFSC lookup service is temporarily unavailable. Please try again.", e
            );
        }
    }

    private String normalizeAndValidate(String ifscCode) {
        if (ifscCode == null) {
            throw new IllegalArgumentException("IFSC code must not be null");
        }
        String normalized = ifscCode.trim().toUpperCase();
        if (!IFSC_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                    "Invalid IFSC code format: " + ifscCode
                            + ". Expected 4 letters, then '0', then 6 alphanumeric characters."
            );
        }
        return normalized;
    }
}
