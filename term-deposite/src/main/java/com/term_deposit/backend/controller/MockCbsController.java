package com.term_deposit.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MockCbsController {

    // Simulates the exact path of the real CBS
    @PostMapping(value = "/mock/api/v3/deposits/td/open", produces = "application/json")
    public ResponseEntity<String> mockOpenTermDeposit() {

        // Generates a unique 8-character string, e.g., MOCK-a1b2c3d4
        String uniqueMockId = "MOCK-" + java.util.UUID.randomUUID().toString().substring(0, 8);

        // Returns a fake success JSON payload with the dynamic ID
        return ResponseEntity.ok("{" +
                "\"status\": \"SUCCESS\"," +
                "\"cbsReferenceNumber\": \"" + uniqueMockId + "\"," +
                "\"appliedInterestRate\": 6.5," +
                "\"maturityDate\": \"2027-09-10\"," +
                "\"maturityAmount\": 53250.00" +
                "}");
    }
}