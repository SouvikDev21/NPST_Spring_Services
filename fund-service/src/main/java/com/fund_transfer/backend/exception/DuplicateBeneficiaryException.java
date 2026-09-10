package com.fund_transfer.backend.exception;

public class DuplicateBeneficiaryException extends RuntimeException {

    public DuplicateBeneficiaryException(String accountNumber, String ifscCode) {
        super("Beneficiary with account " + accountNumber + " and IFSC " + ifscCode + " already exists");
    }
}
