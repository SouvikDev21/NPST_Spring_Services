package com.fund_transfer.backend.ifsc;

import feign.FeignException;
import feign.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class IfscLookupService {

    private static final Logger log = LoggerFactory.getLogger(IfscLookupService.class);

    private static final Pattern IFSC_PATTERN = Pattern.compile("^[A-Z]{4}0[A-Z0-9]{6}$");

    private final IfscFeignClient ifscFeignClient;

    public IfscLookupService(IfscFeignClient ifscFeignClient) {
        this.ifscFeignClient = ifscFeignClient;
    }

    @Cacheable(value = "ifscDetails", key = "#ifscCode")
    public IfscDetailsResponse lookup(String ifscCode) {
        String normalized = normalizeAndValidate(ifscCode);

        try {
            IfscDetailsResponse response = ifscFeignClient.getBranchDetails(normalized);

            if (response == null || response.getIfsc() == null) {
                throw new IfscNotFoundException(normalized);
            }

            return response;

        } catch (FeignException.NotFound e) {
            throw new IfscNotFoundException(normalized);

        } catch (RetryableException e) {
            // Feign's umbrella for connect/read timeouts and connection
            // failures — the equivalent of the old RestClientException catch
            // for network-level problems.
            log.error("IFSC upstream lookup failed (network/timeout) for {}: {}", normalized, e.getMessage());
            throw new IfscLookupUnavailableException(
                    "IFSC lookup service is temporarily unavailable. Please try again.", e
            );

        } catch (FeignException e) {
            // Any other non-2xx status (not a plain 404) from the upstream service.
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
