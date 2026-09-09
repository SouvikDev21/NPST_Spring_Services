package com.fund_transfer.backend.config;

import com.fund_transfer.backend.common.constants.AccountConstants;
import com.fund_transfer.backend.common.constants.CustomerConstants;
import com.fund_transfer.backend.common.constants.LimitConstants;
import com.fund_transfer.backend.entity.*;
import com.fund_transfer.backend.enums.*;
import com.fund_transfer.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CustomerProfileRepository customerProfileRepository;
    private final AccountRepository accountRepository;
    private final AccountTransactionRepository transactionRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final ChannelLimitRepository channelLimitRepository;
    private final CustomerLimitRepository customerLimitRepository;
    private final GlobalLimitRepository globalLimitRepository;

    @Override
    public void run(String... args) {
        log.info("Starting initial seed data loading for H2 database...");

        // 1. Seed Customer Profile
        if (!customerProfileRepository.existsByCustomerId(CustomerConstants.DEFAULT_CUSTOMER_ID)) {
            CustomerProfile profile = CustomerProfile.builder()
                    .customerId(CustomerConstants.DEFAULT_CUSTOMER_ID)
                    .customerName("John Doe")
                    .customerType(CustomerConstants.DEFAULT_CUSTOMER_TYPE)
                    .mobileNumber("9876543210")
                    .emailId("john@email.com")
                    .kycStatus(KycStatus.COMPLETED)
                    .kycVerifiedDate(LocalDate.now().minusMonths(6))
                    .panNumber("ABCDE1234F")
                    .aadhaarMasked("XXXX-XXXX-9012")
                    .address("42 Baker Street, Central District")
                    .dateOfBirth(LocalDate.of(1988, 5, 20))
                    .build();
            customerProfileRepository.save(profile);
            log.info("Seeded CustomerProfile for CIF: {}", CustomerConstants.DEFAULT_CUSTOMER_ID);
        }

        // 2. Seed Accounts
        String defaultAccountNo = "101000000001";
        if (!accountRepository.existsByAccountNumber(defaultAccountNo)) {
            Account account1 = Account.builder()
                    .accountNumber(defaultAccountNo)
                    .customerId(CustomerConstants.DEFAULT_CUSTOMER_ID)
                    .accountType(AccountType.SAVINGS)
                    .status(AccountStatus.ACTIVE)
                    .balance(new BigDecimal("120000.00"))
                    .availableBalance(new BigDecimal("118000.00"))
                    .ledgerBalance(new BigDecimal("120000.00"))
                    .currency(AccountConstants.DEFAULT_CURRENCY)
                    .branchCode(AccountConstants.DEFAULT_BRANCH_CODE)
                    .branchName(AccountConstants.DEFAULT_BRANCH_NAME)
                    .ifscCode(AccountConstants.DEFAULT_IFSC_PREFIX + AccountConstants.DEFAULT_BRANCH_CODE)
                    .micrCode(AccountConstants.DEFAULT_MICR_CODE)
                    .interestRate(AccountConstants.DEFAULT_INTEREST_RATE)
                    .productCode("SB001")
                    .productName("Premium Savings Account")
                    .build();
            accountRepository.save(account1);

            Account account2 = Account.builder()
                    .accountNumber("101000000002")
                    .customerId(CustomerConstants.DEFAULT_CUSTOMER_ID)
                    .accountType(AccountType.CURRENT)
                    .status(AccountStatus.ACTIVE)
                    .balance(new BigDecimal("350000.00"))
                    .availableBalance(new BigDecimal("350000.00"))
                    .ledgerBalance(new BigDecimal("350000.00"))
                    .currency(AccountConstants.DEFAULT_CURRENCY)
                    .branchCode(AccountConstants.DEFAULT_BRANCH_CODE)
                    .branchName(AccountConstants.DEFAULT_BRANCH_NAME)
                    .ifscCode(AccountConstants.DEFAULT_IFSC_PREFIX + AccountConstants.DEFAULT_BRANCH_CODE)
                    .micrCode(AccountConstants.DEFAULT_MICR_CODE)
                    .interestRate(BigDecimal.ZERO)
                    .productCode("CA001")
                    .productName("Business Current Account")
                    .build();
            accountRepository.save(account2);

            log.info("Seeded accounts: {} and {}", defaultAccountNo, "101000000002");

            // 3. Seed Transactions
            AccountTransaction txn1 = AccountTransaction.builder()
                    .transactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .accountNumber(defaultAccountNo)
                    .transactionDate(LocalDate.now().minusDays(2))
                    .valueDate(LocalDate.now().minusDays(2))
                    .transactionType(TransactionType.DEBIT)
                    .amount(new BigDecimal("2500.00"))
                    .currency(AccountConstants.DEFAULT_CURRENCY)
                    .balanceAfter(new BigDecimal("118000.00"))
                    .narration("UPI/Swiggy/Food Order/REF8829")
                    .referenceNo("UPI-88291002")
                    .channel("MOBILE")
                    .build();
            transactionRepository.save(txn1);

            AccountTransaction txn2 = AccountTransaction.builder()
                    .transactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .accountNumber(defaultAccountNo)
                    .transactionDate(LocalDate.now().minusDays(5))
                    .valueDate(LocalDate.now().minusDays(5))
                    .transactionType(TransactionType.CREDIT)
                    .amount(new BigDecimal("5000.00"))
                    .currency(AccountConstants.DEFAULT_CURRENCY)
                    .balanceAfter(new BigDecimal("120500.00"))
                    .narration("IMPS/Transfer from Jane Doe")
                    .referenceNo("IMPS-992812")
                    .channel("INTERNET_BANKING")
                    .build();
            transactionRepository.save(txn2);
        }

        // 4. Seed Beneficiaries
        if (beneficiaryRepository.findByAccountNumber(defaultAccountNo).isEmpty()) {
            Beneficiary ben1 = Beneficiary.builder()
                    .accountNumber(defaultAccountNo)
                    .customerId(CustomerConstants.DEFAULT_CUSTOMER_ID)
                    .beneficiaryName("Jane Doe")
                    .beneficiaryAccountNumber("202000000001")
                    .ifscCode("HDFC0001234")
                    .bankName("HDFC Bank")
                    .transferLimit(new BigDecimal("100000.00"))
                    .status("ACTIVE")
                    .build();
            beneficiaryRepository.save(ben1);

            Beneficiary ben2 = Beneficiary.builder()
                    .accountNumber(defaultAccountNo)
                    .customerId(CustomerConstants.DEFAULT_CUSTOMER_ID)
                    .beneficiaryName("Robert Smith")
                    .beneficiaryAccountNumber("303000000002")
                    .ifscCode("SBIN0005678")
                    .bankName("State Bank of India")
                    .transferLimit(new BigDecimal("50000.00"))
                    .status("ACTIVE")
                    .build();
            beneficiaryRepository.save(ben2);
            log.info("Seeded beneficiaries for account: {}", defaultAccountNo);
        }

        // 5. Seed Channel Limits
        for (LimitChannel ch : List.of(LimitChannel.MOBILE, LimitChannel.INTERNET_BANKING, LimitChannel.ATM, LimitChannel.POS, LimitChannel.UPI)) {
            if (!channelLimitRepository.existsByChannel(ch)) {
                ChannelLimit cl = ChannelLimit.builder()
                        .channel(ch)
                        .perTransactionLimit(LimitConstants.DEFAULT_CHANNEL_TXN_LIMIT)
                        .dailyLimit(LimitConstants.DEFAULT_CHANNEL_DAILY_LIMIT)
                        .monthlyLimit(LimitConstants.DEFAULT_CHANNEL_MONTHLY_LIMIT)
                        .maxTransactionsPerDay(LimitConstants.DEFAULT_CHANNEL_MAX_TXN_PER_DAY)
                        .currency(AccountConstants.DEFAULT_CURRENCY)
                        .status(LimitStatus.ACTIVE)
                        .effectiveFrom(Instant.now())
                        .build();
                channelLimitRepository.save(cl);
            }
        }
        log.info("Seeded channel limits");

        // 6. Seed Customer Limits
        if (customerLimitRepository.findByCustomerIdAndChannel(CustomerConstants.DEFAULT_CUSTOMER_ID, LimitChannel.ALL).isEmpty()) {
            CustomerLimit custLimit = CustomerLimit.builder()
                    .customerId(CustomerConstants.DEFAULT_CUSTOMER_ID)
                    .channel(LimitChannel.ALL)
                    .perTransactionLimit(LimitConstants.DEFAULT_CUSTOMER_TXN_LIMIT)
                    .dailyLimit(LimitConstants.DEFAULT_CUSTOMER_DAILY_LIMIT)
                    .monthlyLimit(LimitConstants.DEFAULT_CUSTOMER_MONTHLY_LIMIT)
                    .maxTransactionsPerDay(LimitConstants.DEFAULT_CUSTOMER_MAX_TXN_PER_DAY)
                    .currency(AccountConstants.DEFAULT_CURRENCY)
                    .status(LimitStatus.ACTIVE)
                    .build();
            customerLimitRepository.save(custLimit);
            log.info("Seeded customer limits for CIF: {}", CustomerConstants.DEFAULT_CUSTOMER_ID);
        }

        // 7. Seed Global Limit Policy
        if (!globalLimitRepository.existsByLimitCode(LimitConstants.GLOBAL_LIMIT_CODE_DEFAULT)) {
            GlobalLimit gl = GlobalLimit.builder()
                    .limitCode(LimitConstants.GLOBAL_LIMIT_CODE_DEFAULT)
                    .limitName(LimitConstants.GLOBAL_LIMIT_NAME_DEFAULT)
                    .perTransactionLimit(LimitConstants.DEFAULT_GLOBAL_TXN_LIMIT)
                    .dailyLimit(LimitConstants.DEFAULT_GLOBAL_DAILY_LIMIT)
                    .monthlyLimit(LimitConstants.DEFAULT_GLOBAL_MONTHLY_LIMIT)
                    .coolingPeriodHours(LimitConstants.DEFAULT_COOLING_PERIOD_HOURS)
                    .currency(AccountConstants.DEFAULT_CURRENCY)
                    .description("Standard default bank-wide transaction policy")
                    .status(LimitStatus.ACTIVE)
                    .build();
            globalLimitRepository.save(gl);
            log.info("Seeded global limits with code: {}", LimitConstants.GLOBAL_LIMIT_CODE_DEFAULT);
        }

        log.info("Seed data loading completed successfully.");
    }
}
