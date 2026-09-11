package com.account_service.backend.service;

import com.account_service.backend.common.constants.AccountConstants;
import com.account_service.backend.common.constants.AppConstants;
import com.account_service.backend.common.exception.ResourceNotFoundException;
import com.account_service.backend.dto.account.AccountDto.*;
import com.account_service.backend.dto.common.PageResponse;
import com.account_service.backend.dto.customer.CustomerDto.JointHolderDto;
import com.account_service.backend.dto.customer.CustomerDto.NomineeDto;
import com.account_service.backend.entity.Account;
import com.account_service.backend.entity.AccountTransaction;
import com.account_service.backend.entity.Beneficiary;
import com.account_service.backend.enums.AccountStatus;
import com.account_service.backend.enums.AccountType;
import com.account_service.backend.enums.TransactionType;
import com.account_service.backend.mapper.AccountMapper;
import com.account_service.backend.repository.AccountRepository;
import com.account_service.backend.repository.AccountTransactionRepository;
import com.account_service.backend.repository.BeneficiaryRepository;
import com.common.cbs.CbsAdapter;
import com.common.cbs.dto.CbsAccountDetailsResponse;
import com.common.cbs.dto.CbsCardInquiryResponse;
import com.common.cbs.dto.CbsStatementResponse;
import com.common.keycloak.KeycloakAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountTransactionRepository transactionRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final AccountMapper accountMapper;
    private final CbsAdapter cbsAdapter;
    private final KeycloakAdapter keycloakAdapter;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AccountResponse> searchAccounts(SearchRequest request) {
        int page = (request != null && request.getPage() != null) ? request.getPage() : AppConstants.DEFAULT_PAGE_NUMBER;
        int size = (request != null && request.getSize() != null) ? request.getSize() : AppConstants.DEFAULT_PAGE_SIZE;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, AppConstants.DEFAULT_SORT_BY));

        String customerId = null;
        if (request != null && request.getCustomerId() != null && !request.getCustomerId().isBlank()) {
            customerId = request.getCustomerId();
        } else {
            customerId = keycloakAdapter.getCifFromCurrentToken().orElse(null);
        }

        String accountNumber = request != null ? request.getAccountNumber() : null;
        AccountType accountType = request != null ? request.getAccountType() : null;
        AccountStatus status = request != null ? request.getStatus() : null;

        log.info("Searching accounts with customerId: {}, accountNumber: {}, type: {}, status: {}",
                customerId, accountNumber, accountType, status);

        Page<Account> accounts = accountRepository.searchAccounts(customerId, accountNumber, accountType, status, pageable);
        List<AccountResponse> content = accounts.getContent().stream()
                .map(accountMapper::toResponse)
                .toList();

        return PageResponse.<AccountResponse>builder()
                .content(content)
                .pageNumber(accounts.getNumber())
                .pageSize(accounts.getSize())
                .totalElements(accounts.getTotalElements())
                .totalPages(accounts.getTotalPages())
                .isLast(accounts.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DetailsResponse getAccountDetails(DetailsRequest request) {
        String accountNumber = request.getAccountNumber();
        log.info("Fetching details for account: {}", accountNumber);

        Optional<CbsAccountDetailsResponse> cbsRespOpt = cbsAdapter.getAccountDetails(accountNumber);
        if (cbsRespOpt.isPresent() && cbsRespOpt.get().getAccount() != null) {
            CbsAccountDetailsResponse cbs = cbsRespOpt.get();
            CbsAccountDetailsResponse.CbsAccountDetailItem item = cbs.getAccount();

            List<DebitCardResponse> cards = new ArrayList<>();
            cbs.getCardList().forEach(c ->
                    cards.add(DebitCardResponse.builder()
                            .cardNumber(c.getCardNumber())
                            .cardType(c.getCardType())
                            .status(c.getStatus())
                            .expiryDate(c.getExpiryDate())
                            .dailyAtmLimit(new BigDecimal("50000.00"))
                            .dailyPosLimit(new BigDecimal("100000.00"))
                            .internationalUsage(false)
                            .contactlessEnabled(true)
                            .build())
            );

            List<JointHolderDto> jointHolders = new ArrayList<>();
            cbs.getRelatedPartiesList().forEach(jh ->
                    jointHolders.add(JointHolderDto.builder()
                            .customerId(jh.getCustomerId())
                            .name(jh.getName())
                            .relationship(jh.getRelationship())
                            .build())
            );

            List<NomineeDto> nominees = new ArrayList<>();
            cbs.getNomineeList().forEach(n ->
                    nominees.add(NomineeDto.builder()
                            .name(n.getName())
                            .relation(n.getRelation())
                            .sharePercentage(n.getSharePercentage() != null ? n.getSharePercentage() : 100)
                            .minor(false)
                            .build())
            );

            LocalDate openDate = null;
            if (item.getOpenDate() != null) {
                try {
                    openDate = LocalDate.parse(item.getOpenDate(), DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (Exception ignored) {}
            }

            return DetailsResponse.builder()
                    .accountNumber(item.getAccountNumber())
                    .customerId(request.getCustomerId())
                    .customerName(null)
                    .accountType(parseAccountType(item.getAccountType()))
                    .status(parseAccountStatus(item.getStatus()))
                    .productCode(item.getProductCode())
                    .productName(item.getProductName())
                    .currency(item.getCurrency() != null ? item.getCurrency() : AccountConstants.DEFAULT_CURRENCY)
                    .balance(item.getLedgerBalance() != null ? item.getLedgerBalance() : BigDecimal.ZERO)
                    .availableBalance(item.getAvailableBalance() != null ? item.getAvailableBalance() : BigDecimal.ZERO)
                    .ledgerBalance(item.getLedgerBalance())
                    .lienAmount(item.getLienAmount() != null ? item.getLienAmount() : BigDecimal.ZERO)
                    .unclearedBalance(item.getUnclearedBalance() != null ? item.getUnclearedBalance() : BigDecimal.ZERO)
                    .interestRate(item.getInterestRate())
                    .branchCode(item.getBranchCode())
                    .branchName(item.getBranchName())
                    .ifscCode(item.getIfsc())
                    .micrCode(item.getMicr())
                    .openDate(openDate)
                    .nomineeRegistered(Boolean.TRUE.equals(item.getNomineeRegistered()))
                    .chequeBookFacility(Boolean.TRUE.equals(item.getChequeBookFacility()))
                    .debitCardActive(Boolean.TRUE.equals(item.getDebitCardActive()))
                    .cards(cards)
                    .jointHolders(jointHolders)
                    .nominees(nominees)
                    .asOf(Instant.now())
                    .build();
        }

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(AccountConstants.ERR_ACCOUNT_NOT_FOUND + accountNumber));

        return accountMapper.toDetailsResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceResponse getAccountBalance(BalanceRequest request) {
        String accountNumber = request.getAccountNumber();
        log.info("Fetching balance for account: {}", accountNumber);

        Optional<CbsAccountDetailsResponse> cbsRespOpt = cbsAdapter.getAccountDetails(accountNumber);
        if (cbsRespOpt.isPresent() && cbsRespOpt.get().getAccount() != null) {
            CbsAccountDetailsResponse.CbsAccountDetailItem item = cbsRespOpt.get().getAccount();
            return BalanceResponse.builder()
                    .accountNumber(item.getAccountNumber())
                    .balance(item.getLedgerBalance() != null ? item.getLedgerBalance() : BigDecimal.ZERO)
                    .availableBalance(item.getAvailableBalance() != null ? item.getAvailableBalance() : BigDecimal.ZERO)
                    .ledgerBalance(item.getLedgerBalance())
                    .currency(item.getCurrency() != null ? item.getCurrency() : AccountConstants.DEFAULT_CURRENCY)
                    .asOf(Instant.now())
                    .build();
        }

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(AccountConstants.ERR_ACCOUNT_NOT_FOUND + accountNumber));
        return accountMapper.toBalanceResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public MiniStatementResponse getMiniStatement(MiniStatementRequest request) {
        String accountNumber = request.getAccountNumber();
        int count = request.getCount() != null ? request.getCount() : AccountConstants.DEFAULT_MINI_STATEMENT_COUNT;
        log.info("Fetching mini-statement for account: {}, count: {}", accountNumber, count);

        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusDays(30);
        Optional<CbsStatementResponse> cbsStmtOpt = cbsAdapter.getAccountStatements(
                accountNumber,
                fromDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                toDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                0,
                count
        );

        if (cbsStmtOpt.isPresent()) {
            List<TransactionResponse> txns = cbsStmtOpt.get().getTransactionList().stream()
                    .limit(count)
                    .map(this::toTransactionResponse)
                    .toList();

            return MiniStatementResponse.builder()
                    .accountNumber(accountNumber)
                    .availableBalance(cbsStmtOpt.get().getClosingBalance())
                    .ledgerBalance(cbsStmtOpt.get().getClosingBalance())
                    .currency(AccountConstants.DEFAULT_CURRENCY)
                    .transactions(txns)
                    .asOf(Instant.now())
                    .build();
        }

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(AccountConstants.ERR_ACCOUNT_NOT_FOUND + accountNumber));

        List<AccountTransaction> dbTxns = transactionRepository.findTop10ByAccountNumberOrderByTransactionDateDescCreatedAtDesc(accountNumber);
        List<TransactionResponse> txns = dbTxns.stream()
                .limit(count)
                .map(this::toTransactionResponse)
                .toList();

        return MiniStatementResponse.builder()
                .accountNumber(accountNumber)
                .availableBalance(account.getAvailableBalance())
                .ledgerBalance(account.getLedgerBalance())
                .currency(account.getCurrency())
                .transactions(txns)
                .asOf(Instant.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> getTransactions(TransactionsRequest request) {
        String accountNumber = request.getAccountNumber();
        int page = (request.getPage() != null) ? request.getPage() : AppConstants.DEFAULT_PAGE_NUMBER;
        int size = (request.getSize() != null) ? request.getSize() : AppConstants.DEFAULT_PAGE_SIZE;

        log.info("Fetching transactions for account: {}, page: {}, size: {}", accountNumber, page, size);

        if (request.getFromDate() != null && request.getToDate() != null) {
            Optional<CbsStatementResponse> cbsStmtOpt = cbsAdapter.getAccountStatements(
                    accountNumber,
                    request.getFromDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    request.getToDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    page * size,
                    size
            );

            if (cbsStmtOpt.isPresent()) {
                List<TransactionResponse> txns = cbsStmtOpt.get().getTransactionList().stream()
                        .map(this::toTransactionResponse)
                        .toList();

                int totalResults = cbsStmtOpt.get().getPaging() != null && cbsStmtOpt.get().getPaging().getTotalResults() != null
                        ? cbsStmtOpt.get().getPaging().getTotalResults()
                        : txns.size();

                return PageResponse.<TransactionResponse>builder()
                        .content(txns)
                        .pageNumber(page)
                        .pageSize(size)
                        .totalElements(totalResults)
                        .totalPages((int) Math.ceil((double) totalResults / size))
                        .isLast((page + 1) * size >= totalResults)
                        .build();
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate", "createdAt"));
        Page<AccountTransaction> dbTxns = transactionRepository.searchTransactions(
                accountNumber, request.getFromDate(), request.getToDate(), request.getTransactionType(), pageable
        );

        List<TransactionResponse> txns = dbTxns.getContent().stream()
                .map(this::toTransactionResponse)
                .toList();

        return PageResponse.<TransactionResponse>builder()
                .content(txns)
                .pageNumber(dbTxns.getNumber())
                .pageSize(dbTxns.getSize())
                .totalElements(dbTxns.getTotalElements())
                .totalPages(dbTxns.getTotalPages())
                .isLast(dbTxns.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> getBeneficiaries(BeneficiariesRequest request) {
        String accountNumber = request != null ? request.getAccountNumber() : null;
        String customerId = request != null ? request.getCustomerId() : null;
        if (customerId == null || customerId.isBlank()) {
            customerId = keycloakAdapter.getCifFromCurrentToken().orElse(null);
        }

        log.info("Fetching beneficiaries for account: {}, customerId: {}", accountNumber, customerId);

        List<Beneficiary> list;
        if (accountNumber != null && !accountNumber.isBlank() && customerId != null && !customerId.isBlank()) {
            list = beneficiaryRepository.findByAccountNumberOrCustomerId(accountNumber, customerId);
        } else if (accountNumber != null && !accountNumber.isBlank()) {
            list = beneficiaryRepository.findByAccountNumber(accountNumber);
        } else if (customerId != null && !customerId.isBlank()) {
            list = beneficiaryRepository.findByCustomerId(customerId);
        } else {
            list = beneficiaryRepository.findAll();
        }

        return list.stream()
                .map(b -> BeneficiaryResponse.builder()
                        .id(b.getId())
                        .accountNumber(b.getAccountNumber())
                        .customerId(b.getCustomerId())
                        .beneficiaryName(b.getBeneficiaryName())
                        .beneficiaryAccountNumber(b.getBeneficiaryAccountNumber())
                        .ifscCode(b.getIfscCode())
                        .bankName(b.getBankName())
                        .transferLimit(b.getTransferLimit())
                        .status(b.getStatus())
                        .createdAt(b.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DebitCardResponse> getLinkedCards(LinkedCardsRequest request) {
        String accountNumber = request.getAccountNumber();
        log.info("Fetching linked cards for account: {}", accountNumber);

        Optional<CbsCardInquiryResponse> cbsCardOpt = cbsAdapter.getLinkedCards(accountNumber);
        if (cbsCardOpt.isPresent() && !cbsCardOpt.get().getCardList().isEmpty()) {
            List<DebitCardResponse> responses = new ArrayList<>();
            cbsCardOpt.get().getCardList().forEach(c ->
                    responses.add(DebitCardResponse.builder()
                            .cardNumber(c.getCardNumber())
                            .cardType(c.getCardType())
                            .status(c.getStatus())
                            .expiryDate(c.getExpiryDate())
                            .dailyAtmLimit(new BigDecimal("50000.00"))
                            .dailyPosLimit(new BigDecimal("100000.00"))
                            .internationalUsage(false)
                            .contactlessEnabled(true)
                            .build())
            );
            return responses;
        }

        return List.of(DebitCardResponse.builder()
                .cardNumber("4591-XXXX-XXXX-1234")
                .cardType("VISA_PLATINUM_DEBIT")
                .status("ACTIVE")
                .expiryDate("2029-12-31")
                .dailyAtmLimit(new BigDecimal("50000.00"))
                .dailyPosLimit(new BigDecimal("100000.00"))
                .internationalUsage(false)
                .contactlessEnabled(true)
                .build());
    }

    @Override
    @Transactional
    public AccountResponse createAccount(CreateRequest request) {
        log.info("Creating account for customer: {}, type: {}", request.getCustomerId(), request.getAccountType());

        String generatedAccountNumber = generateUniqueAccountNumber();
        BigDecimal initialDeposit = request.getInitialDeposit() != null ? request.getInitialDeposit() : AccountConstants.INITIAL_BALANCE_ZERO;
        String currency = request.getCurrency() != null ? request.getCurrency() : AccountConstants.DEFAULT_CURRENCY;
        String branchCode = request.getBranchCode() != null ? request.getBranchCode() : AccountConstants.DEFAULT_BRANCH_CODE;
        String ifscCode = AccountConstants.DEFAULT_IFSC_PREFIX + branchCode;

        Account account = Account.builder()
                .accountNumber(generatedAccountNumber)
                .customerId(request.getCustomerId())
                .accountType(request.getAccountType())
                .status(AccountStatus.ACTIVE)
                .balance(initialDeposit)
                .availableBalance(initialDeposit)
                .ledgerBalance(initialDeposit)
                .currency(currency)
                .branchCode(branchCode)
                .branchName(AccountConstants.DEFAULT_BRANCH_NAME)
                .ifscCode(ifscCode)
                .micrCode(AccountConstants.DEFAULT_MICR_CODE)
                .interestRate(AccountConstants.DEFAULT_INTEREST_RATE)
                .productCode("SB001")
                .productName("Savings Regular Account")
                .build();

        Account saved = accountRepository.save(account);
        log.info("Account created successfully with account number: {}", saved.getAccountNumber());
        return accountMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AccountResponse updateAccountStatus(String accountNumber, UpdateStatusRequest request) {
        log.info("Updating status of account {} to {}", accountNumber, request.getStatus());
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(AccountConstants.ERR_ACCOUNT_NOT_FOUND + accountNumber));

        account.setStatus(request.getStatus());
        Account updated = accountRepository.save(account);
        return accountMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountByAccountNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(AccountConstants.ERR_ACCOUNT_NOT_FOUND + accountNumber));
        return accountMapper.toResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByCustomerId(String customerId) {
        List<Account> accounts = accountRepository.findByCustomerId(customerId);
        return accounts.stream().map(accountMapper::toResponse).toList();
    }

    private String generateUniqueAccountNumber() {
        Random random = new Random();
        String accountNumber;
        do {
            long number = 100000000000L + (long) (random.nextDouble() * 899999999999L);
            accountNumber = String.valueOf(number);
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
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

    private AccountType parseAccountType(String typeStr) {
        if (typeStr == null) return AccountType.SAVINGS;
        try {
            return AccountType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return AccountType.SAVINGS;
        }
    }

    private AccountStatus parseAccountStatus(String statusStr) {
        if (statusStr == null) return AccountStatus.ACTIVE;
        try {
            return AccountStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return AccountStatus.ACTIVE;
        }
    }
}
