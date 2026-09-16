package com.fund_transfer.backend.Npci;




import org.springframework.stereotype.Component;


import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;

@Component

public class MockNpciClient implements NpciClient {


    private static final double FAILURE_RATE = 0.3; // 30% simulated failures

    @Override
    public boolean disburse(String beneficiaryAccountNumber, String ifscCode, BigInteger amount, String idempotencyKey) {
        // Simulate network latency
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return ThreadLocalRandom.current().nextDouble() >= FAILURE_RATE;
    }
}