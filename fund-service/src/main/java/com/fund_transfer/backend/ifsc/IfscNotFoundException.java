package com.fund_transfer.backend.ifsc;

public class IfscNotFoundException extends RuntimeException {

    public IfscNotFoundException(String ifscCode) {
        super("No branch details found for IFSC code: " + ifscCode);
    }
}
