package com.fund_transfer.backend.exception;

public class BeneficiaryNotFoundException extends RuntimeException {

    public BeneficiaryNotFoundException(Long id) {
        super("Beneficiary not found: " + id);
    }
}
