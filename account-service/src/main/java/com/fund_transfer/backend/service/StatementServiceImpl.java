package com.fund_transfer.backend.service;

import com.fund_transfer.backend.adapter.cbs.CbsAdapter;
import com.fund_transfer.backend.adapter.cbs.dto.CbsStatementResponse;
import com.fund_transfer.backend.adapter.keycloak.KeycloakAdapter;
import com.fund_transfer.backend.common.constants.AccountConstants;
import com.fund_transfer.backend.common.constants.AppConstants;
import com.fund_transfer.backend.common.constants.StatementConstants;
import com.fund_transfer.backend.common.exception.ResourceNotFoundException;
import com.fund_transfer.backend.common.exception.StatementException;
import com.fund_transfer.backend.dto.request.statement.StatementDownloadRequest;
import com.fund_transfer.backend.dto.request.statement.StatementEmailRequest;
import com.fund_transfer.backend.dto.request.statement.StatementSearchRequest;
import com.fund_transfer.backend.dto.response.statement.StatementDownloadResponse;
import com.fund_transfer.backend.dto.response.statement.StatementEmailResponse;
import com.fund_transfer.backend.dto.response.statement.StatementSearchResponse;
import com.fund_transfer.backend.dto.response.account.TransactionResponse;
import com.fund_transfer.backend.entity.Account;
import com.fund_transfer.backend.entity.AccountTransaction;
import com.fund_transfer.backend.entity.StatementAudit;
import com.fund_transfer.backend.enums.StatementFormat;
import com.fund_transfer.backend.enums.TransactionType;
import com.fund_transfer.backend.repository.AccountRepository;
import com.fund_transfer.backend.repository.AccountTransactionRepository;
import com.fund_transfer.backend.repository.StatementAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatementServiceImpl implements StatementService {

    private final AccountRepository accountRepository;
    private final AccountTransactionRepository transactionRepository;
    private final StatementAuditRepository statementAuditRepository;
    private final CbsAdapter cbsAdapter;
    private final KeycloakAdapter keycloakAdapter;

    @Override
    @Transactional(readOnly = true)
    public StatementSearchResponse searchStatements(StatementSearchRequest request) {
        String accountNumber = request.getAccountNumber();
        LocalDate fromDate = request.getFromDate() != null ? request.getFromDate() : LocalDate.now().minusDays(30);
        LocalDate toDate = request.getToDate() != null ? request.getToDate() : LocalDate.now();

        if (fromDate.isAfter(toDate)) {
            throw new StatementException(StatementConstants.ERR_INVALID_DATE_RANGE);
        }

        int page = request.getPage() != null ? request.getPage() : AppConstants.DEFAULT_PAGE_NUMBER;
        int size = request.getSize() != null ? request.getSize() : AppConstants.DEFAULT_PAGE_SIZE;

        log.info("Searching statements for account: {}, from: {}, to: {}", accountNumber, fromDate, toDate);

        // Try CBS statements
        Optional<CbsStatementResponse> cbsStmtOpt = cbsAdapter.getAccountStatements(
                accountNumber,
                fromDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                toDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                page * size,
                size
        );

        if (cbsStmtOpt.isPresent() && cbsStmtOpt.get().getTransactions() != null) {
            CbsStatementResponse cbs = cbsStmtOpt.get();
            List<CbsStatementResponse.CbsTransactionRecord> records = new ArrayList<>();
            cbs.getTransactions().values().forEach(records::addAll);

            List<TransactionResponse> txns = records.stream()
                    .map(this::toTransactionResponse)
                    .toList();

            int total = cbs.getPaging() != null && cbs.getPaging().getTotalResults() != null
                    ? cbs.getPaging().getTotalResults()
                    : txns.size();

            return StatementSearchResponse.builder()
                    .accountId(accountNumber)
                    .fromDate(fromDate)
                    .toDate(toDate)
                    .openingBalance(cbs.getOpeningBalance())
                    .closingBalance(cbs.getClosingBalance())
                    .totalDebits(cbs.getTotalDebits())
                    .totalCredits(cbs.getTotalCredits())
                    .currency(AccountConstants.DEFAULT_CURRENCY)
                    .transactions(txns)
                    .totalResults(total)
                    .offset(page * size)
                    .limit(size)
                    .build();
        }

        // Fallback to local DB
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(AccountConstants.ERR_ACCOUNT_NOT_FOUND + accountNumber));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate"));
        Page<AccountTransaction> txnsPage = transactionRepository.searchTransactions(
                accountNumber, fromDate, toDate, null, pageable
        );

        List<TransactionResponse> txns = txnsPage.getContent().stream()
                .map(this::toTransactionResponse)
                .toList();

        BigDecimal debits = txnsPage.getContent().stream()
                .filter(t -> t.getTransactionType() == TransactionType.DEBIT)
                .map(AccountTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal credits = txnsPage.getContent().stream()
                .filter(t -> t.getTransactionType() == TransactionType.CREDIT)
                .map(AccountTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return StatementSearchResponse.builder()
                .accountId(accountNumber)
                .fromDate(fromDate)
                .toDate(toDate)
                .openingBalance(account.getBalance().subtract(credits).add(debits))
                .closingBalance(account.getBalance())
                .totalDebits(debits)
                .totalCredits(credits)
                .currency(account.getCurrency())
                .transactions(txns)
                .totalResults((int) txnsPage.getTotalElements())
                .offset(page * size)
                .limit(size)
                .build();
    }

    @Override
    @Transactional
    public StatementDownloadResponse downloadStatement(StatementDownloadRequest request) {
        String accountNumber = request.getAccountNumber();
        LocalDate fromDate = request.getFromDate() != null ? request.getFromDate() : LocalDate.now().minusDays(30);
        LocalDate toDate = request.getToDate() != null ? request.getToDate() : LocalDate.now();
        StatementFormat format = request.getFormat() != null ? request.getFormat() : StatementFormat.PDF;

        log.info("Generating statement download for account: {}, format: {}", accountNumber, format);

        String refNo = "STMT-DL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String fileContent = "ACCOUNT STATEMENT\nAccount: " + accountNumber + "\nFrom: " + fromDate + "\nTo: " + toDate + "\nFormat: " + format + "\nGenerated: " + Instant.now();
        String base64 = Base64.getEncoder().encodeToString(fileContent.getBytes(StandardCharsets.UTF_8));

        String contentType = switch (format) {
            case CSV -> StatementConstants.CONTENT_TYPE_CSV;
            case EXCEL -> StatementConstants.CONTENT_TYPE_EXCEL;
            case PDF -> StatementConstants.CONTENT_TYPE_PDF;
        };

        String extension = format.name().toLowerCase();
        String fileName = "statement_" + accountNumber + "_" + fromDate + "_to_" + toDate + "." + extension;

        // Audit statement download
        StatementAudit audit = StatementAudit.builder()
                .accountNumber(accountNumber)
                .customerId(keycloakAdapter.getCifFromCurrentToken().orElse(null))
                .actionType("DOWNLOAD")
                .fromDate(fromDate)
                .toDate(toDate)
                .format(format)
                .status(StatementConstants.STATUS_GENERATED)
                .referenceNumber(refNo)
                .timestamp(Instant.now())
                .build();
        statementAuditRepository.save(audit);

        return StatementDownloadResponse.builder()
                .accountNumber(accountNumber)
                .fromDate(fromDate)
                .toDate(toDate)
                .format(format)
                .fileName(fileName)
                .contentType(contentType)
                .fileContentBase64(base64)
                .fileSizeBytes((long) fileContent.getBytes(StandardCharsets.UTF_8).length)
                .generatedAt(Instant.now())
                .build();
    }

    @Override
    @Transactional
    public StatementEmailResponse emailStatement(StatementEmailRequest request) {
        String accountNumber = request.getAccountNumber();
        String email = request.getEmailAddress();
        LocalDate fromDate = request.getFromDate() != null ? request.getFromDate() : LocalDate.now().minusDays(30);
        LocalDate toDate = request.getToDate() != null ? request.getToDate() : LocalDate.now();
        StatementFormat format = request.getFormat() != null ? request.getFormat() : StatementFormat.PDF;

        log.info("Dispatching statement email for account: {} to: {}", accountNumber, email);

        String refNo = "STMT-EML-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        StatementAudit audit = StatementAudit.builder()
                .accountNumber(accountNumber)
                .customerId(keycloakAdapter.getCifFromCurrentToken().orElse(null))
                .actionType("EMAIL")
                .fromDate(fromDate)
                .toDate(toDate)
                .format(format)
                .recipientEmail(email)
                .status(StatementConstants.STATUS_DISPATCHED)
                .referenceNumber(refNo)
                .timestamp(Instant.now())
                .build();
        statementAuditRepository.save(audit);

        return StatementEmailResponse.builder()
                .accountNumber(accountNumber)
                .recipientEmail(email)
                .referenceNumber(refNo)
                .status(StatementConstants.STATUS_DISPATCHED)
                .message(StatementConstants.MSG_STATEMENT_DISPATCHED + email)
                .dispatchedAt(Instant.now())
                .build();
    }

    private TransactionResponse toTransactionResponse(AccountTransaction t) {
        return TransactionResponse.builder()
                .transactionId(t.getTransactionId())
                .accountNumber(t.getAccountNumber())
                .transactionDate(t.getTransactionDate())
                .valueDate(t.getValueDate())
                .transactionType(t.getTransactionType())
                .amount(t.getAmount())
                .currency(t.getCurrency())
                .balanceAfter(t.getBalanceAfter())
                .narration(t.getNarration())
                .referenceNo(t.getReferenceNo())
                .channel(t.getChannel())
                .createdAt(t.getCreatedAt())
                .build();
    }

    private TransactionResponse toTransactionResponse(CbsStatementResponse.CbsTransactionRecord c) {
        LocalDate tDate = null;
        LocalDate vDate = null;
        try {
            if (c.getTransactionDate() != null) {
                tDate = LocalDate.parse(c.getTransactionDate(), DateTimeFormatter.ISO_LOCAL_DATE);
            }
            if (c.getValueDate() != null) {
                vDate = LocalDate.parse(c.getValueDate(), DateTimeFormatter.ISO_LOCAL_DATE);
            }
        } catch (Exception ignored) {}

        TransactionType type = "CREDIT".equalsIgnoreCase(c.getType()) ? TransactionType.CREDIT : TransactionType.DEBIT;

        return TransactionResponse.builder()
                .transactionId(c.getTransactionId())
                .accountNumber(null)
                .transactionDate(tDate)
                .valueDate(vDate)
                .transactionType(type)
                .amount(c.getAmount())
                .currency(c.getCurrency())
                .balanceAfter(c.getBalanceAfter())
                .narration(c.getNarration())
                .referenceNo(c.getReferenceNo())
                .channel(c.getChannel())
                .createdAt(Instant.now())
                .build();
    }
}
