package com.fund_transfer.backend.Npci;



import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Profile({"local", "dev", "test"})
public class MockNpciClient implements NpciClient {

    // Tune this to control how often the mock simulates an NPCI failure,
    // so you can exercise the reversal path (Step 5) during testing.
    private static final double FAILURE_RATE = 0.2; // 20% simulated failures

    @Override
    public boolean disburse(String beneficiaryAccountNumber, String ifscCode, BigDecimal amount, String idempotencyKey) {
        // Simulate network latency
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return ThreadLocalRandom.current().nextDouble() >= FAILURE_RATE;
    }
}