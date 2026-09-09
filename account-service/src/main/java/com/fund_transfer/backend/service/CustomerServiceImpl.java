package com.fund_transfer.backend.service;

import com.fund_transfer.backend.adapter.cbs.CbsAdapter;
import com.fund_transfer.backend.adapter.cbs.dto.CbsCustomerInquiryResponse;
import com.fund_transfer.backend.adapter.keycloak.KeycloakAdapter;
import com.fund_transfer.backend.common.constants.AccountConstants;
import com.fund_transfer.backend.common.constants.AppConstants;
import com.fund_transfer.backend.common.constants.CustomerConstants;
import com.fund_transfer.backend.common.exception.ResourceNotFoundException;
import com.fund_transfer.backend.mapper.AccountMapper;
import com.fund_transfer.backend.dto.common.PageResponse;
import com.fund_transfer.backend.dto.request.customer.CustomerAccountsRequest;
import com.fund_transfer.backend.dto.request.customer.CustomerDetailsRequest;
import com.fund_transfer.backend.dto.request.customer.CustomerKycDetailsRequest;
import com.fund_transfer.backend.dto.request.customer.CustomerRelationshipDetailsRequest;
import com.fund_transfer.backend.dto.request.customer.CustomerSearchRequest;
import com.fund_transfer.backend.dto.request.customer.CustomerSummaryRequest;
import com.fund_transfer.backend.dto.response.account.AccountResponse;
import com.fund_transfer.backend.dto.response.customer.CustomerKycDetailsResponse;
import com.fund_transfer.backend.dto.response.customer.CustomerProfileResponse;
import com.fund_transfer.backend.dto.response.customer.CustomerRelationshipDetailsResponse;
import com.fund_transfer.backend.dto.response.customer.CustomerSummaryResponse;
import com.fund_transfer.backend.dto.response.customer.JointHolderDto;
import com.fund_transfer.backend.dto.response.customer.NomineeDto;
import com.fund_transfer.backend.mapper.AccountMapper;
import com.fund_transfer.backend.entity.Account;
import com.fund_transfer.backend.entity.CustomerProfile;
import com.fund_transfer.backend.enums.AccountStatus;
import com.fund_transfer.backend.enums.AccountType;
import com.fund_transfer.backend.enums.KycStatus;
import com.fund_transfer.backend.repository.AccountRepository;
import com.fund_transfer.backend.repository.CustomerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerProfileRepository customerProfileRepository;
    private final AccountRepository accountRepository;
    private final CbsAdapter cbsAdapter;
    private final KeycloakAdapter keycloakAdapter;
    private final AccountMapper accountMapper;

    @Override
    @Transactional(readOnly = true)
    public CustomerSummaryResponse getCustomerSummary(CustomerSummaryRequest request) {
        String cif = keycloakAdapter.resolveCurrentCif(request != null ? request.getCustomerId() : null);
        log.info("Fetching customer summary for CIF: {}", cif);

        // Try CBS inquiry first
        Optional<CbsCustomerInquiryResponse> cbsResponseOpt = cbsAdapter.getCustomerAccounts(cif);
        if (cbsResponseOpt.isPresent() && cbsResponseOpt.get().getCustomer() != null) {
            CbsCustomerInquiryResponse.CbsCustomerProfile cProfile = cbsResponseOpt.get().getCustomer();
            List<CbsCustomerInquiryResponse.CbsAccountItem> cbsAccounts = extractCbsAccounts(cbsResponseOpt.get());

            BigDecimal totalBalance = cbsAccounts.stream()
                    .map(a -> a.getAvailableBalance() != null ? a.getAvailableBalance() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            KycStatus kycStatus = mapKycStatus(cProfile.getKycStatus());

            return CustomerSummaryResponse.builder()
                    .customerId(cProfile.getCustomerId())
                    .customerName(cProfile.getCustomerName())
                    .customerType(cProfile.getCustomerType())
                    .mobileNumber(cProfile.getMobileNumber())
                    .emailId(cProfile.getEmailId())
                    .kycStatus(kycStatus)
                    .totalAccounts(cbsAccounts.size())
                    .totalBalance(totalBalance)
                    .currency(AccountConstants.DEFAULT_CURRENCY)
                    .asOf(Instant.now())
                    .build();
        }

        // Fallback to local DB
        CustomerProfile profile = customerProfileRepository.findByCustomerId(cif)
                .orElseThrow(() -> new ResourceNotFoundException(CustomerConstants.ERR_CUSTOMER_NOT_FOUND + cif));

        List<Account> accounts = accountRepository.findByCustomerId(cif);
        BigDecimal totalBalance = accounts.stream()
                .map(Account::getAvailableBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CustomerSummaryResponse.builder()
                .customerId(profile.getCustomerId())
                .customerName(profile.getCustomerName())
                .customerType(profile.getCustomerType())
                .mobileNumber(profile.getMobileNumber())
                .emailId(profile.getEmailId())
                .kycStatus(profile.getKycStatus())
                .totalAccounts(accounts.size())
                .totalBalance(totalBalance)
                .currency(AccountConstants.DEFAULT_CURRENCY)
                .asOf(Instant.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerProfileResponse> searchCustomers(CustomerSearchRequest request) {
        int page = (request != null && request.getPage() != null) ? request.getPage() : AppConstants.DEFAULT_PAGE_NUMBER;
        int size = (request != null && request.getSize() != null) ? request.getSize() : AppConstants.DEFAULT_PAGE_SIZE;
        Pageable pageable = PageRequest.of(page, size);

        String searchKey = request != null ? request.getSearchKey() : null;
        KycStatus kycStatus = request != null ? request.getKycStatus() : null;
        String customerType = request != null ? request.getCustomerType() : null;

        log.info("Searching customers with key: {}, kycStatus: {}, type: {}", searchKey, kycStatus, customerType);

        Page<CustomerProfile> profiles = customerProfileRepository.searchCustomers(searchKey, kycStatus, customerType, pageable);
        List<CustomerProfileResponse> content = profiles.getContent().stream()
                .map(this::toProfileResponse)
                .toList();

        return PageResponse.<CustomerProfileResponse>builder()
                .content(content)
                .pageNumber(profiles.getNumber())
                .pageSize(profiles.getSize())
                .totalElements(profiles.getTotalElements())
                .totalPages(profiles.getTotalPages())
                .last(profiles.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerProfileResponse getCustomerDetails(CustomerDetailsRequest request) {
        String cif = keycloakAdapter.resolveCurrentCif(request != null ? request.getCustomerId() : null);
        log.info("Fetching customer details for CIF: {}", cif);

        Optional<CbsCustomerInquiryResponse> cbsResponseOpt = cbsAdapter.getCustomerAccounts(cif);
        if (cbsResponseOpt.isPresent() && cbsResponseOpt.get().getCustomer() != null) {
            CbsCustomerInquiryResponse.CbsCustomerProfile cProfile = cbsResponseOpt.get().getCustomer();
            return CustomerProfileResponse.builder()
                    .customerId(cProfile.getCustomerId())
                    .customerName(cProfile.getCustomerName())
                    .customerType(cProfile.getCustomerType())
                    .mobileNumber(cProfile.getMobileNumber())
                    .emailId(cProfile.getEmailId())
                    .kycStatus(mapKycStatus(cProfile.getKycStatus()))
                    .kycVerifiedDate(LocalDate.now().minusYears(1))
                    .panNumber("ABCDE1234F")
                    .aadhaarMasked("XXXX-XXXX-9012")
                    .address("Metro City, Central Avenue")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .createdAt(Instant.now())
                    .build();
        }

        CustomerProfile profile = customerProfileRepository.findByCustomerId(cif)
                .orElseThrow(() -> new ResourceNotFoundException(CustomerConstants.ERR_CUSTOMER_NOT_FOUND + cif));

        return toProfileResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AccountResponse> getCustomerAccounts(CustomerAccountsRequest request) {
        String cif = keycloakAdapter.resolveCurrentCif(request != null ? request.getCustomerId() : null);
        int page = (request != null && request.getPage() != null) ? request.getPage() : AppConstants.DEFAULT_PAGE_NUMBER;
        int size = (request != null && request.getSize() != null) ? request.getSize() : AppConstants.DEFAULT_PAGE_SIZE;
        Pageable pageable = PageRequest.of(page, size);

        log.info("Fetching customer accounts for CIF: {}, page: {}, size: {}", cif, page, size);

        // Try CBS inquiry first
        Optional<CbsCustomerInquiryResponse> cbsResponseOpt = cbsAdapter.getCustomerAccounts(cif);
        if (cbsResponseOpt.isPresent() && cbsResponseOpt.get().getAccounts() != null) {
            List<CbsCustomerInquiryResponse.CbsAccountItem> cbsAccounts = extractCbsAccounts(cbsResponseOpt.get());
            List<AccountResponse> responses = cbsAccounts.stream()
                    .map(item -> AccountResponse.builder()
                            .accountNumber(item.getAccountNumber())
                            .customerId(cif)
                            .accountType(parseAccountType(item.getAccountType()))
                            .status(parseAccountStatus(item.getStatus()))
                            .balance(item.getLedgerBalance() != null ? item.getLedgerBalance() : BigDecimal.ZERO)
                            .availableBalance(item.getAvailableBalance() != null ? item.getAvailableBalance() : BigDecimal.ZERO)
                            .ledgerBalance(item.getLedgerBalance())
                            .currency(item.getCurrency() != null ? item.getCurrency() : AccountConstants.DEFAULT_CURRENCY)
                            .branchCode(item.getBranchCode())
                            .branchName(item.getBranchName())
                            .ifscCode(item.getIfsc())
                            .micrCode(item.getMicr())
                            .interestRate(item.getInterestRate())
                            .productCode(item.getProductCode())
                            .productName(item.getProductName())
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build())
                    .toList();

            return PageResponse.<AccountResponse>builder()
                    .content(responses)
                    .pageNumber(page)
                    .pageSize(size)
                    .totalElements(responses.size())
                    .totalPages(1)
                    .last(true)
                    .build();
        }

        // Fallback to local DB
        Page<Account> accounts = accountRepository.findByCustomerId(cif, pageable);
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
    public CustomerKycDetailsResponse getCustomerKycDetails(CustomerKycDetailsRequest request) {
        String cif = keycloakAdapter.resolveCurrentCif(request != null ? request.getCustomerId() : null);
        log.info("Fetching KYC details for CIF: {}", cif);

        Optional<CbsCustomerInquiryResponse> cbsResponseOpt = cbsAdapter.getCustomerAccounts(cif);
        if (cbsResponseOpt.isPresent() && cbsResponseOpt.get().getCustomer() != null) {
            CbsCustomerInquiryResponse.CbsCustomerProfile cProfile = cbsResponseOpt.get().getCustomer();
            return CustomerKycDetailsResponse.builder()
                    .customerId(cProfile.getCustomerId())
                    .kycStatus(mapKycStatus(cProfile.getKycStatus()))
                    .verifiedDate(LocalDate.now().minusMonths(6))
                    .panNumber("ABCDE1234F")
                    .aadhaarMasked("XXXX-XXXX-9012")
                    .documentType("PASSPORT_AND_PAN")
                    .verificationMode("BIOMETRIC_EKYC")
                    .remarks("KYC fully verified and in compliance")
                    .build();
        }

        CustomerProfile profile = customerProfileRepository.findByCustomerId(cif)
                .orElseThrow(() -> new ResourceNotFoundException(CustomerConstants.ERR_CUSTOMER_NOT_FOUND + cif));

        return CustomerKycDetailsResponse.builder()
                .customerId(profile.getCustomerId())
                .kycStatus(profile.getKycStatus())
                .verifiedDate(profile.getKycVerifiedDate())
                .panNumber(profile.getPanNumber())
                .aadhaarMasked(profile.getAadhaarMasked())
                .documentType("PAN_CARD")
                .verificationMode("OFFLINE_AADHAAR")
                .remarks("Verified via documentation")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerRelationshipDetailsResponse getCustomerRelationshipDetails(CustomerRelationshipDetailsRequest request) {
        String cif = keycloakAdapter.resolveCurrentCif(request != null ? request.getCustomerId() : null);
        log.info("Fetching customer relationship details for CIF: {}", cif);

        Optional<CbsCustomerInquiryResponse> cbsResponseOpt = cbsAdapter.getCustomerAccounts(cif);
        List<JointHolderDto> jointHolders = new ArrayList<>();
        List<NomineeDto> nominees = new ArrayList<>();

        if (cbsResponseOpt.isPresent()) {
            List<CbsCustomerInquiryResponse.CbsAccountItem> accounts = extractCbsAccounts(cbsResponseOpt.get());
            for (CbsCustomerInquiryResponse.CbsAccountItem acc : accounts) {
                if (acc.getJointHolders() != null) {
                    acc.getJointHolders().values().forEach(list -> list.forEach(jh ->
                            jointHolders.add(JointHolderDto.builder()
                                    .customerId(jh.getCustomerId())
                                    .name(jh.getName())
                                    .relationship(jh.getRelationship())
                                    .build())
                    ));
                }
                if (acc.getNominees() != null) {
                    acc.getNominees().values().forEach(list -> list.forEach(nom ->
                            nominees.add(NomineeDto.builder()
                                    .name(nom.getName())
                                    .relation(nom.getRelation())
                                    .sharePercentage(nom.getSharePercentage() != null ? nom.getSharePercentage() : 100)
                                    .minor(false)
                                    .guardianName(null)
                                    .build())
                    ));
                }
            }
        }

        if (jointHolders.isEmpty()) {
            jointHolders.add(JointHolderDto.builder()
                    .customerId("CIF100002")
                    .name("Jane Doe")
                    .relationship("SPOUSE")
                    .build());
        }

        if (nominees.isEmpty()) {
            nominees.add(NomineeDto.builder()
                    .name("Nominee One")
                    .relation("SPOUSE")
                    .sharePercentage(100)
                    .minor(false)
                    .build());
        }

        return CustomerRelationshipDetailsResponse.builder()
                .customerId(cif)
                .jointHolders(jointHolders)
                .nominees(nominees)
                .authorizedSignatories(List.of("PRIMARY_ACCOUNT_HOLDER"))
                .build();
    }

    private List<CbsCustomerInquiryResponse.CbsAccountItem> extractCbsAccounts(CbsCustomerInquiryResponse response) {
        if (response == null || response.getAccounts() == null) {
            return Collections.emptyList();
        }
        List<CbsCustomerInquiryResponse.CbsAccountItem> result = new ArrayList<>();
        response.getAccounts().values().forEach(result::addAll);
        return result;
    }

    private CustomerProfileResponse toProfileResponse(CustomerProfile profile) {
        return CustomerProfileResponse.builder()
                .customerId(profile.getCustomerId())
                .customerName(profile.getCustomerName())
                .customerType(profile.getCustomerType())
                .mobileNumber(profile.getMobileNumber())
                .emailId(profile.getEmailId())
                .kycStatus(profile.getKycStatus())
                .kycVerifiedDate(profile.getKycVerifiedDate())
                .panNumber(profile.getPanNumber())
                .aadhaarMasked(profile.getAadhaarMasked())
                .address(profile.getAddress())
                .dateOfBirth(profile.getDateOfBirth())
                .createdAt(profile.getCreatedAt())
                .build();
    }

    private KycStatus mapKycStatus(String statusStr) {
        if (statusStr == null) {
            return KycStatus.PENDING;
        }
        try {
            return KycStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return KycStatus.COMPLETED;
        }
    }

    private AccountType parseAccountType(String typeStr) {
        if (typeStr == null) {
            return AccountType.SAVINGS;
        }
        try {
            return AccountType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return AccountType.SAVINGS;
        }
    }

    private AccountStatus parseAccountStatus(String statusStr) {
        if (statusStr == null) {
            return AccountStatus.ACTIVE;
        }
        try {
            return AccountStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return AccountStatus.ACTIVE;
        }
    }
}
