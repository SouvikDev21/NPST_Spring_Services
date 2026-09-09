package com.fund_transfer.backend.service;

import com.fund_transfer.backend.adapter.keycloak.KeycloakAdapter;
import com.fund_transfer.backend.common.constants.AppConstants;
import com.fund_transfer.backend.common.constants.LimitConstants;
import com.fund_transfer.backend.common.exception.LimitException;
import com.fund_transfer.backend.common.exception.ResourceNotFoundException;
import com.fund_transfer.backend.dto.common.PageResponse;
import com.fund_transfer.backend.dto.request.limit.ChannelLimitDetailsRequest;
import com.fund_transfer.backend.dto.request.limit.ChannelLimitHistoryRequest;
import com.fund_transfer.backend.dto.request.limit.ChannelLimitSearchRequest;
import com.fund_transfer.backend.dto.request.limit.CreateChannelLimitRequest;
import com.fund_transfer.backend.dto.request.limit.CreateCustomerLimitRequest;
import com.fund_transfer.backend.dto.request.limit.CreateGlobalLimitRequest;
import com.fund_transfer.backend.dto.request.limit.CustomerLimitDetailsRequest;
import com.fund_transfer.backend.dto.request.limit.CustomerLimitHistoryRequest;
import com.fund_transfer.backend.dto.request.limit.CustomerLimitSearchRequest;
import com.fund_transfer.backend.dto.request.limit.GlobalLimitDetailsRequest;
import com.fund_transfer.backend.dto.request.limit.GlobalLimitHistoryRequest;
import com.fund_transfer.backend.dto.request.limit.GlobalLimitSearchRequest;
import com.fund_transfer.backend.dto.request.limit.UpdateChannelLimitRequest;
import com.fund_transfer.backend.dto.request.limit.UpdateCustomerLimitRequest;
import com.fund_transfer.backend.dto.request.limit.UpdateGlobalLimitRequest;
import com.fund_transfer.backend.dto.response.limit.ChannelLimitHistoryResponse;
import com.fund_transfer.backend.dto.response.limit.ChannelLimitResponse;
import com.fund_transfer.backend.dto.response.limit.CustomerLimitHistoryResponse;
import com.fund_transfer.backend.dto.response.limit.CustomerLimitResponse;
import com.fund_transfer.backend.dto.response.limit.GlobalLimitHistoryResponse;
import com.fund_transfer.backend.dto.response.limit.GlobalLimitResponse;
import com.fund_transfer.backend.entity.*;
import com.fund_transfer.backend.enums.LimitAction;
import com.fund_transfer.backend.enums.LimitChannel;
import com.fund_transfer.backend.enums.LimitStatus;
import com.fund_transfer.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LimitServiceImpl implements LimitService {

    private final ChannelLimitRepository channelLimitRepository;
    private final ChannelLimitHistoryRepository channelLimitHistoryRepository;
    private final CustomerLimitRepository customerLimitRepository;
    private final CustomerLimitHistoryRepository customerLimitHistoryRepository;
    private final GlobalLimitRepository globalLimitRepository;
    private final GlobalLimitHistoryRepository globalLimitHistoryRepository;
    private final KeycloakAdapter keycloakAdapter;

    // ==========================================
    // 1. Channel Limits
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ChannelLimitResponse> searchChannelLimits(ChannelLimitSearchRequest request) {
        int page = (request != null && request.getPage() != null) ? request.getPage() : AppConstants.DEFAULT_PAGE_NUMBER;
        int size = (request != null && request.getSize() != null) ? request.getSize() : AppConstants.DEFAULT_PAGE_SIZE;
        Pageable pageable = PageRequest.of(page, size);

        LimitChannel channel = request != null ? request.getChannel() : null;
        LimitStatus status = request != null ? request.getStatus() : null;

        log.info("Searching channel limits for channel: {}, status: {}", channel, status);

        Page<ChannelLimit> limits;
        if (channel != null && status != null) {
            limits = channelLimitRepository.findByChannelAndStatus(channel, status, pageable);
        } else if (status != null) {
            limits = channelLimitRepository.findByStatus(status, pageable);
        } else {
            limits = channelLimitRepository.findAll(pageable);
        }

        List<ChannelLimitResponse> content = limits.getContent().stream()
                .map(this::toChannelLimitResponse)
                .toList();

        return PageResponse.<ChannelLimitResponse>builder()
                .content(content)
                .pageNumber(limits.getNumber())
                .pageSize(limits.getSize())
                .totalElements(limits.getTotalElements())
                .totalPages(limits.getTotalPages())
                .last(limits.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ChannelLimitResponse getChannelLimitDetails(ChannelLimitDetailsRequest request) {
        if (request == null || (request.getLimitId() == null && request.getChannel() == null)) {
            throw new LimitException(LimitConstants.ERR_CHANNEL_REQUIRED);
        }

        log.info("Fetching channel limit details by id: {} or channel: {}", request.getLimitId(), request.getChannel());

        ChannelLimit limit;
        if (request.getLimitId() != null) {
            limit = channelLimitRepository.findById(request.getLimitId())
                    .orElseThrow(() -> new ResourceNotFoundException(LimitConstants.ERR_LIMIT_NOT_FOUND + request.getLimitId()));
        } else {
            limit = channelLimitRepository.findByChannel(request.getChannel())
                    .orElseThrow(() -> new ResourceNotFoundException(LimitConstants.ERR_LIMIT_NOT_FOUND + request.getChannel()));
        }

        return toChannelLimitResponse(limit);
    }

    @Override
    @Transactional
    public ChannelLimitResponse createChannelLimit(CreateChannelLimitRequest request) {
        log.info("Creating channel limit for: {}", request.getChannel());

        if (channelLimitRepository.existsByChannel(request.getChannel())) {
            throw new LimitException(LimitConstants.ERR_LIMIT_ALREADY_EXISTS + request.getChannel());
        }

        String currency = request.getCurrency() != null ? request.getCurrency() : AppConstants.DEFAULT_CURRENCY;
        int maxTxn = request.getMaxTransactionsPerDay() != null ? request.getMaxTransactionsPerDay() : LimitConstants.DEFAULT_CHANNEL_MAX_TXN_PER_DAY;

        ChannelLimit limit = ChannelLimit.builder()
                .channel(request.getChannel())
                .perTransactionLimit(request.getPerTransactionLimit())
                .dailyLimit(request.getDailyLimit())
                .monthlyLimit(request.getMonthlyLimit())
                .maxTransactionsPerDay(maxTxn)
                .currency(currency)
                .status(LimitStatus.ACTIVE)
                .effectiveFrom(Instant.now())
                .build();

        ChannelLimit saved = channelLimitRepository.save(limit);

        // Record history
        recordChannelHistory(saved, LimitAction.CREATE, request.getRemarks());

        return toChannelLimitResponse(saved);
    }

    @Override
    @Transactional
    public ChannelLimitResponse updateChannelLimit(UpdateChannelLimitRequest request) {
        log.info("Updating channel limit for id: {} or channel: {}", request.getLimitId(), request.getChannel());

        ChannelLimit limit;
        if (request.getLimitId() != null) {
            limit = channelLimitRepository.findById(request.getLimitId())
                    .orElseThrow(() -> new ResourceNotFoundException(LimitConstants.ERR_LIMIT_NOT_FOUND + request.getLimitId()));
        } else if (request.getChannel() != null) {
            limit = channelLimitRepository.findByChannel(request.getChannel())
                    .orElseThrow(() -> new ResourceNotFoundException(LimitConstants.ERR_LIMIT_NOT_FOUND + request.getChannel()));
        } else {
            throw new LimitException(LimitConstants.ERR_CHANNEL_REQUIRED);
        }

        if (request.getPerTransactionLimit() != null) {
            limit.setPerTransactionLimit(request.getPerTransactionLimit());
        }
        if (request.getDailyLimit() != null) {
            limit.setDailyLimit(request.getDailyLimit());
        }
        if (request.getMonthlyLimit() != null) {
            limit.setMonthlyLimit(request.getMonthlyLimit());
        }
        if (request.getMaxTransactionsPerDay() != null) {
            limit.setMaxTransactionsPerDay(request.getMaxTransactionsPerDay());
        }
        if (request.getStatus() != null) {
            limit.setStatus(request.getStatus());
        }

        ChannelLimit updated = channelLimitRepository.save(limit);

        // Record history
        recordChannelHistory(updated, LimitAction.UPDATE, request.getRemarks());

        return toChannelLimitResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ChannelLimitHistoryResponse> getChannelLimitHistory(ChannelLimitHistoryRequest request) {
        int page = (request != null && request.getPage() != null) ? request.getPage() : AppConstants.DEFAULT_PAGE_NUMBER;
        int size = (request != null && request.getSize() != null) ? request.getSize() : AppConstants.DEFAULT_PAGE_SIZE;
        Pageable pageable = PageRequest.of(page, size);

        Page<ChannelLimitHistory> historyPage;
        if (request != null && request.getChannel() != null) {
            historyPage = channelLimitHistoryRepository.findByChannelOrderByTimestampDesc(request.getChannel(), pageable);
        } else {
            historyPage = channelLimitHistoryRepository.findAllByOrderByTimestampDesc(pageable);
        }

        List<ChannelLimitHistoryResponse> content = historyPage.getContent().stream()
                .map(h -> ChannelLimitHistoryResponse.builder()
                        .id(h.getId())
                        .channel(h.getChannel())
                        .perTransactionLimit(h.getPerTransactionLimit())
                        .dailyLimit(h.getDailyLimit())
                        .monthlyLimit(h.getMonthlyLimit())
                        .maxTransactionsPerDay(h.getMaxTransactionsPerDay())
                        .action(h.getAction())
                        .performedBy(h.getPerformedBy())
                        .remarks(h.getRemarks())
                        .timestamp(h.getTimestamp())
                        .build())
                .toList();

        return PageResponse.<ChannelLimitHistoryResponse>builder()
                .content(content)
                .pageNumber(historyPage.getNumber())
                .pageSize(historyPage.getSize())
                .totalElements(historyPage.getTotalElements())
                .totalPages(historyPage.getTotalPages())
                .last(historyPage.isLast())
                .build();
    }

    // ==========================================
    // 2. Customer Limits
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerLimitResponse> searchCustomerLimits(CustomerLimitSearchRequest request) {
        int page = (request != null && request.getPage() != null) ? request.getPage() : AppConstants.DEFAULT_PAGE_NUMBER;
        int size = (request != null && request.getSize() != null) ? request.getSize() : AppConstants.DEFAULT_PAGE_SIZE;
        Pageable pageable = PageRequest.of(page, size);

        String customerId = null;
        if (request != null && request.getCustomerId() != null && !request.getCustomerId().isBlank()) {
            customerId = request.getCustomerId();
        } else {
            customerId = keycloakAdapter.getCifFromCurrentToken().orElse(null);
        }

        LimitChannel channel = request != null ? request.getChannel() : null;
        LimitStatus status = request != null ? request.getStatus() : null;

        log.info("Searching customer limits for customer: {}, channel: {}, status: {}", customerId, channel, status);

        Page<CustomerLimit> limits = customerLimitRepository.searchCustomerLimits(customerId, channel, status, pageable);
        List<CustomerLimitResponse> content = limits.getContent().stream()
                .map(this::toCustomerLimitResponse)
                .toList();

        return PageResponse.<CustomerLimitResponse>builder()
                .content(content)
                .pageNumber(limits.getNumber())
                .pageSize(limits.getSize())
                .totalElements(limits.getTotalElements())
                .totalPages(limits.getTotalPages())
                .last(limits.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerLimitResponse getCustomerLimitDetails(CustomerLimitDetailsRequest request) {
        if (request == null) {
            throw new LimitException(LimitConstants.ERR_CUSTOMER_REQUIRED);
        }

        log.info("Fetching customer limit details for request: {}", request);

        if (request.getLimitId() != null) {
            CustomerLimit limit = customerLimitRepository.findById(request.getLimitId())
                    .orElseThrow(() -> new ResourceNotFoundException(LimitConstants.ERR_LIMIT_NOT_FOUND + request.getLimitId()));
            return toCustomerLimitResponse(limit);
        }

        String customerId = keycloakAdapter.resolveCurrentCif(request.getCustomerId());
        LimitChannel channel = request.getChannel() != null ? request.getChannel() : LimitChannel.ALL;

        CustomerLimit limit = customerLimitRepository.findByCustomerIdAndChannel(customerId, channel)
                .orElseThrow(() -> new ResourceNotFoundException(LimitConstants.ERR_LIMIT_NOT_FOUND + customerId + "/" + channel));

        return toCustomerLimitResponse(limit);
    }

    @Override
    @Transactional
    public CustomerLimitResponse createCustomerLimit(CreateCustomerLimitRequest request) {
        String customerId = keycloakAdapter.resolveCurrentCif(request.getCustomerId());
        log.info("Creating customer limit for customer: {}, channel: {}", customerId, request.getChannel());

        if (customerLimitRepository.findByCustomerIdAndChannel(customerId, request.getChannel()).isPresent()) {
            throw new LimitException(LimitConstants.ERR_LIMIT_ALREADY_EXISTS + customerId + " - " + request.getChannel());
        }

        String currency = request.getCurrency() != null ? request.getCurrency() : AppConstants.DEFAULT_CURRENCY;
        int maxTxn = request.getMaxTransactionsPerDay() != null ? request.getMaxTransactionsPerDay() : LimitConstants.DEFAULT_CUSTOMER_MAX_TXN_PER_DAY;

        CustomerLimit limit = CustomerLimit.builder()
                .customerId(customerId)
                .channel(request.getChannel())
                .perTransactionLimit(request.getPerTransactionLimit())
                .dailyLimit(request.getDailyLimit())
                .monthlyLimit(request.getMonthlyLimit())
                .maxTransactionsPerDay(maxTxn)
                .currency(currency)
                .status(LimitStatus.ACTIVE)
                .build();

        CustomerLimit saved = customerLimitRepository.save(limit);

        recordCustomerHistory(saved, LimitAction.CREATE, request.getRemarks());

        return toCustomerLimitResponse(saved);
    }

    @Override
    @Transactional
    public CustomerLimitResponse updateCustomerLimit(UpdateCustomerLimitRequest request) {
        CustomerLimit limit;
        if (request.getLimitId() != null) {
            limit = customerLimitRepository.findById(request.getLimitId())
                    .orElseThrow(() -> new ResourceNotFoundException(LimitConstants.ERR_LIMIT_NOT_FOUND + request.getLimitId()));
        } else {
            String customerId = keycloakAdapter.resolveCurrentCif(request.getCustomerId());
            LimitChannel channel = request.getChannel() != null ? request.getChannel() : LimitChannel.ALL;
            limit = customerLimitRepository.findByCustomerIdAndChannel(customerId, channel)
                    .orElseThrow(() -> new ResourceNotFoundException(LimitConstants.ERR_LIMIT_NOT_FOUND + customerId + "/" + channel));
        }

        if (request.getPerTransactionLimit() != null) {
            limit.setPerTransactionLimit(request.getPerTransactionLimit());
        }
        if (request.getDailyLimit() != null) {
            limit.setDailyLimit(request.getDailyLimit());
        }
        if (request.getMonthlyLimit() != null) {
            limit.setMonthlyLimit(request.getMonthlyLimit());
        }
        if (request.getMaxTransactionsPerDay() != null) {
            limit.setMaxTransactionsPerDay(request.getMaxTransactionsPerDay());
        }
        if (request.getStatus() != null) {
            limit.setStatus(request.getStatus());
        }

        CustomerLimit updated = customerLimitRepository.save(limit);

        recordCustomerHistory(updated, LimitAction.UPDATE, request.getRemarks());

        return toCustomerLimitResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerLimitHistoryResponse> getCustomerLimitHistory(CustomerLimitHistoryRequest request) {
        String customerId = keycloakAdapter.resolveCurrentCif(request != null ? request.getCustomerId() : null);
        int page = (request != null && request.getPage() != null) ? request.getPage() : AppConstants.DEFAULT_PAGE_NUMBER;
        int size = (request != null && request.getSize() != null) ? request.getSize() : AppConstants.DEFAULT_PAGE_SIZE;
        Pageable pageable = PageRequest.of(page, size);

        Page<CustomerLimitHistory> historyPage;
        if (request != null && request.getChannel() != null) {
            historyPage = customerLimitHistoryRepository.findByCustomerIdAndChannelOrderByTimestampDesc(customerId, request.getChannel(), pageable);
        } else {
            historyPage = customerLimitHistoryRepository.findByCustomerIdOrderByTimestampDesc(customerId, pageable);
        }

        List<CustomerLimitHistoryResponse> content = historyPage.getContent().stream()
                .map(h -> CustomerLimitHistoryResponse.builder()
                        .id(h.getId())
                        .customerId(h.getCustomerId())
                        .channel(h.getChannel())
                        .perTransactionLimit(h.getPerTransactionLimit())
                        .dailyLimit(h.getDailyLimit())
                        .monthlyLimit(h.getMonthlyLimit())
                        .maxTransactionsPerDay(h.getMaxTransactionsPerDay())
                        .action(h.getAction())
                        .performedBy(h.getPerformedBy())
                        .remarks(h.getRemarks())
                        .timestamp(h.getTimestamp())
                        .build())
                .toList();

        return PageResponse.<CustomerLimitHistoryResponse>builder()
                .content(content)
                .pageNumber(historyPage.getNumber())
                .pageSize(historyPage.getSize())
                .totalElements(historyPage.getTotalElements())
                .totalPages(historyPage.getTotalPages())
                .last(historyPage.isLast())
                .build();
    }

    // ==========================================
    // 3. Global / Admin Limits
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GlobalLimitResponse> searchGlobalLimits(GlobalLimitSearchRequest request) {
        int page = (request != null && request.getPage() != null) ? request.getPage() : AppConstants.DEFAULT_PAGE_NUMBER;
        int size = (request != null && request.getSize() != null) ? request.getSize() : AppConstants.DEFAULT_PAGE_SIZE;
        Pageable pageable = PageRequest.of(page, size);

        String limitCode = request != null ? request.getLimitCode() : null;
        LimitStatus status = request != null ? request.getStatus() : null;

        log.info("Searching global limits with limitCode: {}, status: {}", limitCode, status);

        Page<GlobalLimit> limits = globalLimitRepository.searchGlobalLimits(limitCode, status, pageable);
        List<GlobalLimitResponse> content = limits.getContent().stream()
                .map(this::toGlobalLimitResponse)
                .toList();

        return PageResponse.<GlobalLimitResponse>builder()
                .content(content)
                .pageNumber(limits.getNumber())
                .pageSize(limits.getSize())
                .totalElements(limits.getTotalElements())
                .totalPages(limits.getTotalPages())
                .last(limits.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GlobalLimitResponse getGlobalLimitDetails(GlobalLimitDetailsRequest request) {
        if (request == null || (request.getLimitId() == null && request.getLimitCode() == null)) {
            throw new LimitException(LimitConstants.ERR_GLOBAL_CODE_REQUIRED);
        }

        log.info("Fetching global limit details for: {}", request);

        GlobalLimit limit;
        if (request.getLimitId() != null) {
            limit = globalLimitRepository.findById(request.getLimitId())
                    .orElseThrow(() -> new ResourceNotFoundException(LimitConstants.ERR_LIMIT_NOT_FOUND + request.getLimitId()));
        } else {
            limit = globalLimitRepository.findByLimitCode(request.getLimitCode())
                    .orElseThrow(() -> new ResourceNotFoundException(LimitConstants.ERR_LIMIT_NOT_FOUND + request.getLimitCode()));
        }

        return toGlobalLimitResponse(limit);
    }

    @Override
    @Transactional
    public GlobalLimitResponse createGlobalLimit(CreateGlobalLimitRequest request) {
        log.info("Creating global limit policy: {}", request.getLimitCode());

        if (globalLimitRepository.existsByLimitCode(request.getLimitCode())) {
            throw new LimitException(LimitConstants.ERR_LIMIT_ALREADY_EXISTS + request.getLimitCode());
        }

        String currency = request.getCurrency() != null ? request.getCurrency() : AppConstants.DEFAULT_CURRENCY;
        int coolingHours = request.getCoolingPeriodHours() != null ? request.getCoolingPeriodHours() : LimitConstants.DEFAULT_COOLING_PERIOD_HOURS;

        GlobalLimit limit = GlobalLimit.builder()
                .limitCode(request.getLimitCode())
                .limitName(request.getLimitName())
                .perTransactionLimit(request.getPerTransactionLimit())
                .dailyLimit(request.getDailyLimit())
                .monthlyLimit(request.getMonthlyLimit())
                .coolingPeriodHours(coolingHours)
                .currency(currency)
                .description(request.getDescription())
                .status(LimitStatus.ACTIVE)
                .build();

        GlobalLimit saved = globalLimitRepository.save(limit);

        recordGlobalHistory(saved, LimitAction.CREATE, "Initial global policy creation");

        return toGlobalLimitResponse(saved);
    }

    @Override
    @Transactional
    public GlobalLimitResponse updateGlobalLimit(UpdateGlobalLimitRequest request) {
        log.info("Updating global limit for: {}", request);

        GlobalLimit limit;
        if (request.getLimitId() != null) {
            limit = globalLimitRepository.findById(request.getLimitId())
                    .orElseThrow(() -> new ResourceNotFoundException(LimitConstants.ERR_LIMIT_NOT_FOUND + request.getLimitId()));
        } else if (request.getLimitCode() != null) {
            limit = globalLimitRepository.findByLimitCode(request.getLimitCode())
                    .orElseThrow(() -> new ResourceNotFoundException(LimitConstants.ERR_LIMIT_NOT_FOUND + request.getLimitCode()));
        } else {
            throw new LimitException(LimitConstants.ERR_GLOBAL_CODE_REQUIRED);
        }

        if (request.getPerTransactionLimit() != null) {
            limit.setPerTransactionLimit(request.getPerTransactionLimit());
        }
        if (request.getDailyLimit() != null) {
            limit.setDailyLimit(request.getDailyLimit());
        }
        if (request.getMonthlyLimit() != null) {
            limit.setMonthlyLimit(request.getMonthlyLimit());
        }
        if (request.getCoolingPeriodHours() != null) {
            limit.setCoolingPeriodHours(request.getCoolingPeriodHours());
        }
        if (request.getStatus() != null) {
            limit.setStatus(request.getStatus());
        }
        if (request.getDescription() != null) {
            limit.setDescription(request.getDescription());
        }

        GlobalLimit updated = globalLimitRepository.save(limit);

        recordGlobalHistory(updated, LimitAction.UPDATE, "Updated global limit policy");

        return toGlobalLimitResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GlobalLimitHistoryResponse> getGlobalLimitHistory(GlobalLimitHistoryRequest request) {
        int page = (request != null && request.getPage() != null) ? request.getPage() : AppConstants.DEFAULT_PAGE_NUMBER;
        int size = (request != null && request.getSize() != null) ? request.getSize() : AppConstants.DEFAULT_PAGE_SIZE;
        Pageable pageable = PageRequest.of(page, size);

        Page<GlobalLimitHistory> historyPage;
        if (request != null && request.getLimitCode() != null && !request.getLimitCode().isBlank()) {
            historyPage = globalLimitHistoryRepository.findByLimitCodeOrderByTimestampDesc(request.getLimitCode(), pageable);
        } else {
            historyPage = globalLimitHistoryRepository.findAllByOrderByTimestampDesc(pageable);
        }

        List<GlobalLimitHistoryResponse> content = historyPage.getContent().stream()
                .map(h -> GlobalLimitHistoryResponse.builder()
                        .id(h.getId())
                        .limitCode(h.getLimitCode())
                        .perTransactionLimit(h.getPerTransactionLimit())
                        .dailyLimit(h.getDailyLimit())
                        .monthlyLimit(h.getMonthlyLimit())
                        .action(h.getAction())
                        .performedBy(h.getPerformedBy())
                        .remarks(h.getRemarks())
                        .timestamp(h.getTimestamp())
                        .build())
                .toList();

        return PageResponse.<GlobalLimitHistoryResponse>builder()
                .content(content)
                .pageNumber(historyPage.getNumber())
                .pageSize(historyPage.getSize())
                .totalElements(historyPage.getTotalElements())
                .totalPages(historyPage.getTotalPages())
                .last(historyPage.isLast())
                .build();
    }

    // ==========================================
    // Helpers
    // ==========================================

    private void recordChannelHistory(ChannelLimit limit, LimitAction action, String remarks) {
        String user = keycloakAdapter.getCurrentUserContext().getUsername();
        ChannelLimitHistory history = ChannelLimitHistory.builder()
                .channel(limit.getChannel())
                .perTransactionLimit(limit.getPerTransactionLimit())
                .dailyLimit(limit.getDailyLimit())
                .monthlyLimit(limit.getMonthlyLimit())
                .maxTransactionsPerDay(limit.getMaxTransactionsPerDay())
                .action(action)
                .performedBy(user)
                .remarks(remarks)
                .timestamp(Instant.now())
                .build();
        channelLimitHistoryRepository.save(history);
    }

    private void recordCustomerHistory(CustomerLimit limit, LimitAction action, String remarks) {
        String user = keycloakAdapter.getCurrentUserContext().getUsername();
        CustomerLimitHistory history = CustomerLimitHistory.builder()
                .customerId(limit.getCustomerId())
                .channel(limit.getChannel())
                .perTransactionLimit(limit.getPerTransactionLimit())
                .dailyLimit(limit.getDailyLimit())
                .monthlyLimit(limit.getMonthlyLimit())
                .maxTransactionsPerDay(limit.getMaxTransactionsPerDay())
                .action(action)
                .performedBy(user)
                .remarks(remarks)
                .timestamp(Instant.now())
                .build();
        customerLimitHistoryRepository.save(history);
    }

    private void recordGlobalHistory(GlobalLimit limit, LimitAction action, String remarks) {
        String user = keycloakAdapter.getCurrentUserContext().getUsername();
        GlobalLimitHistory history = GlobalLimitHistory.builder()
                .limitCode(limit.getLimitCode())
                .perTransactionLimit(limit.getPerTransactionLimit())
                .dailyLimit(limit.getDailyLimit())
                .monthlyLimit(limit.getMonthlyLimit())
                .action(action)
                .performedBy(user)
                .remarks(remarks)
                .timestamp(Instant.now())
                .build();
        globalLimitHistoryRepository.save(history);
    }

    private ChannelLimitResponse toChannelLimitResponse(ChannelLimit l) {
        return ChannelLimitResponse.builder()
                .id(l.getId())
                .channel(l.getChannel())
                .perTransactionLimit(l.getPerTransactionLimit())
                .dailyLimit(l.getDailyLimit())
                .monthlyLimit(l.getMonthlyLimit())
                .maxTransactionsPerDay(l.getMaxTransactionsPerDay())
                .currency(l.getCurrency())
                .status(l.getStatus())
                .effectiveFrom(l.getEffectiveFrom())
                .effectiveTo(l.getEffectiveTo())
                .createdAt(l.getCreatedAt())
                .updatedAt(l.getUpdatedAt())
                .build();
    }

    private CustomerLimitResponse toCustomerLimitResponse(CustomerLimit l) {
        return CustomerLimitResponse.builder()
                .id(l.getId())
                .customerId(l.getCustomerId())
                .channel(l.getChannel())
                .perTransactionLimit(l.getPerTransactionLimit())
                .dailyLimit(l.getDailyLimit())
                .monthlyLimit(l.getMonthlyLimit())
                .maxTransactionsPerDay(l.getMaxTransactionsPerDay())
                .currency(l.getCurrency())
                .status(l.getStatus())
                .createdAt(l.getCreatedAt())
                .updatedAt(l.getUpdatedAt())
                .build();
    }

    private GlobalLimitResponse toGlobalLimitResponse(GlobalLimit l) {
        return GlobalLimitResponse.builder()
                .id(l.getId())
                .limitCode(l.getLimitCode())
                .limitName(l.getLimitName())
                .perTransactionLimit(l.getPerTransactionLimit())
                .dailyLimit(l.getDailyLimit())
                .monthlyLimit(l.getMonthlyLimit())
                .coolingPeriodHours(l.getCoolingPeriodHours())
                .currency(l.getCurrency())
                .description(l.getDescription())
                .status(l.getStatus())
                .createdAt(l.getCreatedAt())
                .updatedAt(l.getUpdatedAt())
                .build();
    }
}
