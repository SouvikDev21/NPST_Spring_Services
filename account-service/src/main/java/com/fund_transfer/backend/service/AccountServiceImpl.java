package com.fund_transfer.backend.service;

import com.fund_transfer.backend.adapter.cbs.CbsAdapter;
import com.fund_transfer.backend.adapter.cbs.dto.CbsAccountDetailsResponse;
import com.fund_transfer.backend.adapter.cbs.dto.CbsCardInquiryResponse;
import com.fund_transfer.backend.adapter.cbs.dto.CbsStatementResponse;
import com.fund_transfer.backend.adapter.keycloak.KeycloakAdapter;
import com.fund_transfer.backend.common.constants.AccountConstants;
import com.fund_transfer.backend.common.constants.AppConstants;
import com.fund_transfer.backend.common.exception.ResourceNotFoundException;
import com.fund_transfer.backend.mapper.AccountMapper;
import com.fund_transfer.backend.dto.common.PageResponse;
import com.fund_transfer.backend.dto.request.account.AccountBalanceRequest;
import com.fund_transfer.backend.dto.request.account.AccountBeneficiariesRequest;
import com.fund_transfer.backend.dto.request.account.AccountDetailsRequest;
import com.fund_transfer.backend.dto.request.account.AccountLinkedCardsRequest;
import com.fund_transfer.backend.dto.request.account.AccountMiniStatementRequest;
import com.fund_transfer.backend.dto.request.account.AccountSearchRequest;
import com.fund_transfer.backend.dto.request.account.AccountTransactionsRequest;
import com.fund_transfer.backend.dto.request.account.CreateAccountRequest;
import com.fund_transfer.backend.dto.request.account.UpdateAccountStatusRequest;
import com.fund_transfer.backend.dto.response.account.AccountBalanceResponse;
import com.fund_transfer.backend.dto.response.account.AccountDetailsResponse;
import com.fund_transfer.backend.dto.response.account.AccountMiniStatementResponse;
import com.fund_transfer.backend.dto.response.account.AccountResponse;
import com.fund_transfer.backend.dto.response.account.BeneficiaryResponse;
import com.fund_transfer.backend.dto.response.account.DebitCardResponse;
import com.fund_transfer.backend.dto.response.account.TransactionResponse;
import com.fund_transfer.backend.dto.response.customer.JointHolderDto;
import com.fund_transfer.backend.dto.response.customer.NomineeDto;
import com.fund_transfer.backend.mapper.AccountMapper;
import com.fund_transfer.backend.entity.Account;
import com.fund_transfer.backend.entity.AccountTransaction;
import com.fund_transfer.backend.entity.Beneficiary;
import com.fund_transfer.backend.enums.AccountStatus;
import com.fund_transfer.backend.enums.AccountType;
import com.fund_transfer.backend.enums.TransactionType;
import com.fund_transfer.backend.repository.AccountRepository;
import com.fund_transfer.backend.repository.AccountTransactionRepository;
import com.fund_transfer.backend.repository.BeneficiaryRepository;
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
    public PageResponse<AccountResponse> searchAccounts(AccountSearchRequest request) {
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
                .last(accounts.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDetailsResponse getAccountDetails(AccountDetailsRequest request) {
        String accountNumber = request.getAccountNumber();
        log.info("Fetching details for account: {}", accountNumber);

        // Try CBS details inquiry
        Optional<CbsAccountDetailsResponse> cbsRespOpt = cbsAdapter.getAccountDetails(accountNumber);
        if (cbsRespOpt.isPresent() && cbsRespOpt.get().getAccount() != null) {
            CbsAccountDetailsResponse cbs = cbsRespOpt.get();
            CbsAccountDetailsResponse.CbsAccountDetailItem item = cbs.getAccount();

            List<DebitCardResponse> cards = new ArrayList<>();
            if (cbs.getCards() != null) {
                cbs.getCards().values().forEach(cardList -> cardList.forEach(c ->
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
                ));
            }

            List<JointHolderDto> jointHolders = new ArrayList<>();
            if (cbs.getRelatedParties() != null) {
                cbs.getRelatedParties().values().forEach(jList -> jList.forEach(jh ->
                        jointHolders.add(JointHolderDto.builder()
                                .customerId(jh.getCustomerId())
                                .name(jh.getName())
                                .relationship(jh.getRelationship())
                                .build())
                ));
            }

            List<NomineeDto> nominees = new ArrayList<>();
            if (cbs.getNominees() != null) {
                cbs.getNominees().values().forEach(nList -> nList.forEach(n ->
                        nominees.add(NomineeDto.builder()
                                .name(n.getName())
                                .relation(n.getRelation())
                                .sharePercentage(n.getSharePercentage() != null ? n.getSharePercentage() : 100)
                                .minor(false)
                                .build())
                ));
            }

            LocalDate openDate = null;
            if (item.getOpenDate() != null) {
                try {
                    openDate = LocalDate.parse(item.getOpenDate(), DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (Exception ignored) {}
            }

            return AccountDetailsResponse.builder()
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

        // Fallback to local DB
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(AccountConstants.ERR_ACCOUNT_NOT_FOUND + accountNumber));

        return accountMapper.toDetailsResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountBalanceResponse getAccountBalance(AccountBalanceRequest request) {
        String accountNumber = request.getAccountNumber();
        log.info("Fetching balance for account: {}", accountNumber);

        Optional<CbsAccountDetailsResponse> cbsRespOpt = cbsAdapter.getAccountDetails(accountNumber);
        if (cbsRespOpt.isPresent() && cbsRespOpt.get().getAccount() != null) {
            CbsAccountDetailsResponse.CbsAccountDetailItem item = cbsRespOpt.get().getAccount();
            return AccountBalanceResponse.builder()
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
    public AccountMiniStatementResponse getMiniStatement(AccountMiniStatementRequest request) {
        String accountNumber = request.getAccountNumber();
        int count = request.getCount() != null ? request.getCount() : AccountConstants.DEFAULT_MINI_STATEMENT_COUNT;
        log.info("Fetching mini-statement for account: {}, count: {}", accountNumber, count);

        // Try CBS statements
        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusDays(30);
        Optional<CbsStatementResponse> cbsStmtOpt = cbsAdapter.getAccountStatements(
                accountNumber,
                fromDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                toDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                0,
                count
        );

        if (cbsStmtOpt.isPresent() && cbsStmtOpt.get().getTransactions() != null) {
            List<CbsStatementResponse.CbsTransactionRecord> cbsTxns = new ArrayList<>();
            cbsStmtOpt.get().getTransactions().values().forEach(cbsTxns::addAll);

            List<TransactionResponse> txns = cbsTxns.stream()
                    .limit(count)
                    .map(this::toTransactionResponse)
                    .toList();

            return AccountMiniStatementResponse.builder()
                    .accountNumber(accountNumber)
                    .availableBalance(cbsStmtOpt.get().getClosingBalance())
                    .ledgerBalance(cbsStmtOpt.get().getClosingBalance())
                    .currency(AccountConstants.DEFAULT_CURRENCY)
                    .transactions(txns)
                    .asOf(Instant.now())
                    .build();
        }

        // Fallback to local DB
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(AccountConstants.ERR_ACCOUNT_NOT_FOUND + accountNumber));

        List<AccountTransaction> dbTxns = transactionRepository.findTop10ByAccountNumberOrderByTransactionDateDescCreatedAtDesc(accountNumber);
        List<TransactionResponse> txns = dbTxns.stream()
                .limit(count)
                .map(this::toTransactionResponse)
                .toList();

        return AccountMiniStatementResponse.builder()
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
    public PageResponse<TransactionResponse> getTransactions(AccountTransactionsRequest request) {
        String accountNumber = request.getAccountNumber();
        int page = (request.getPage() != null) ? request.getPage() : AppConstants.DEFAULT_PAGE_NUMBER;
        int size = (request.getSize() != null) ? request.getSize() : AppConstants.DEFAULT_PAGE_SIZE;

        log.info("Fetching transactions for account: {}, page: {}, size: {}", accountNumber, page, size);

        // Try CBS statements
        if (request.getFromDate() != null && request.getToDate() != null) {
            Optional<CbsStatementResponse> cbsStmtOpt = cbsAdapter.getAccountStatements(
                    accountNumber,
                    request.getFromDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    request.getToDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    page * size,
                    size
            );

            if (cbsStmtOpt.isPresent() && cbsStmtOpt.get().getTransactions() != null) {
                List<CbsStatementResponse.CbsTransactionRecord> cbsTxns = new ArrayList<>();
                cbsStmtOpt.get().getTransactions().values().forEach(cbsTxns::addAll);
                List<TransactionResponse> list = cbsTxns.stream().map(this::toTransactionResponse).toList();
                int total = cbsStmtOpt.get().getPaging() != null ? cbsStmtOpt.get().getPaging().getTotalResults() : list.size();

                return PageResponse.<TransactionResponse>builder()
                        .content(list)
                        .pageNumber(page)
                        .pageSize(size)
                        .totalElements(total)
                        .totalPages((int) Math.ceil((double) total / size))
                        .last((page + 1) * size >= total)
                        .build();
            }
        }

        // Fallback to local DB
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate"));
        Page<AccountTransaction> txns = transactionRepository.searchTransactions(
                accountNumber,
                request.getFromDate(),
                request.getToDate(),
                request.getTransactionType(),
                pageable
        );

        List<TransactionResponse> content = txns.getContent().stream()
                .map(this::toTransactionResponse)
                .toList();

        return PageResponse.<TransactionResponse>builder()
                .content(content)
                .pageNumber(txns.getNumber())
                .pageSize(txns.getSize())
                .totalElements(txns.getTotalElements())
                .totalPages(txns.getTotalPages())
                .last(txns.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> getBeneficiaries(AccountBeneficiariesRequest request) {
        String accountNumber = request != null ? request.getAccountNumber() : null;
        String customerId = request != null ? request.getCustomerId() : null;

        if ((customerId == null || customerId.isBlank()) && (accountNumber == null || accountNumber.isBlank())) {
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
    public List<DebitCardResponse> getLinkedCards(AccountLinkedCardsRequest request) {
        String accountNumber = request.getAccountNumber();
        log.info("Fetching linked cards for account: {}", accountNumber);

        Optional<CbsCardInquiryResponse> cbsCardOpt = cbsAdapter.getLinkedCards(accountNumber);
        if (cbsCardOpt.isPresent() && cbsCardOpt.get().getCards() != null) {
            List<DebitCardResponse> responses = new ArrayList<>();
            cbsCardOpt.get().getCards().values().forEach(cardList -> cardList.forEach(c ->
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
            ));
            if (!responses.isEmpty()) {
                return responses;
            }
        }

        // Return standard debit card for local account
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
    public AccountResponse createAccount(CreateAccountRequest request) {
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
    public AccountResponse updateAccountStatus(String accountNumber, UpdateAccountStatusRequest request) {
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
