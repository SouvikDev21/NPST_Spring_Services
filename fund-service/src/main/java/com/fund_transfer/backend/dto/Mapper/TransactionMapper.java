package com.fund_transfer.backend.dto.Mapper;

import com.fund_transfer.backend.dto.Response.TransactionResponse;
import com.fund_transfer.backend.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {

        if (transaction == null) {
            return null;
        }

        return TransactionResponse.builder()
                .transactionReference(transaction.getTransactionReference())
                .status(transaction.getStatus())
                .amountMinorUnits(transaction.getAmountMinorUnits())
                .failureReason(transaction.getFailureReason())
                .completedAt(transaction.getUpdatedAt())
                .build();
    }
}