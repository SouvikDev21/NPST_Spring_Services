package com.fund_transfer.backend.service;

import com.fund_transfer.backend.dto.Request.TransferRequest;
import com.fund_transfer.backend.dto.Response.TransactionResponse;
import com.fund_transfer.backend.entity.Transaction;
import com.fund_transfer.backend.enums.TransactionStatus;
import com.fund_transfer.backend.dto.Mapper.TransactionMapper;
import com.fund_transfer.backend.repository.TransactionRepo;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepo transactionRepo;
    private final TransactionMapper transactionMapper;



    public TransactionResponse processTransfer(TransferRequest request) {
        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionReference(UUID.randomUUID().toString())
                .idempotencyKey(request.idempotencyKey() != null ? request.idempotencyKey() : UUID.randomUUID().toString())
                .initiatorCif(request.initiatorCif())
                .initiatorKeycloakUserId(request.initiatorKeycloakUserId())
                .beneficiaryId(request.beneficiaryId())
                .destinationAccountNumber(request.destinationAccountNumber())
                .destinationIfscCode(request.destinationIfscCode())
                .amountMinorUnits(request.amountMinorUnits())
                .currency(request.currency())
                .transferMode(request.transferMode())
                .status(TransactionStatus.INITIATED)
                .bankCode(request.bankCode())
                .initiatedAt(Instant.now())
                .build();

        Transaction savedTransaction = transactionRepo.save(transaction);
        return transactionMapper.toResponse(savedTransaction);
    }
}
