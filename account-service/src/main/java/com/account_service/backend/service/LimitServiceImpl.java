package com.account_service.backend.service;

import com.account_service.backend.common.constants.AppConstants;
import com.account_service.backend.common.constants.LimitConstants;
import com.account_service.backend.common.exception.LimitException;
import com.account_service.backend.common.exception.ResourceNotFoundException;
import com.account_service.backend.dto.common.PageResponse;
import com.account_service.backend.dto.limit.LimitDto.*;
import com.account_service.backend.entity.*;
import com.account_service.backend.enums.LimitAction;
import com.account_service.backend.enums.LimitChannel;
import com.account_service.backend.enums.LimitStatus;
import com.account_service.backend.repository.*;
import com.common.keycloak.KeycloakAdapter;
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
    // Channel Limits
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ChannelResponse> searchChannelLimits(ChannelSearchRequest request) {
        int page = (request != null && request.getPage() != null) ? request.getPage() : AppConstants.DEFAULT_PAGE_NUMBER;
        int size = (request != null && request.getSize() != null) ? request.getSize() : AppConstants.DEFAULT_PAGE_SIZE;
        Pageable pageable = PageRequest.of(page, size);

        LimitChannel channel = request != null ? request.getChannel() : null;
        log.info("Searching channel limits for channel: {}, page: {}, size: {}", channel, page, size);

        Page<ChannelLimit> limitPage;
        if (channel != null) {
            limitPage = channelLimitRepository.findByChannel(channel, pageable);
        } else {
            limitPage = channelLimitRepository.findAll(pageable);
        }

        List<ChannelResponse> content = limitPage.getContent().stream()
                .map(this::toChannelLimitResponse)
                .toList();

        return PageResponse.<ChannelResponse>builder()
                .content(content)
                .pageNumber(limitPage.getNumber())
                .pageSize(limitPage.getSize())
                .totalElements(limitPage.getTotalElements())
                .totalPages(limitPage.getTotalPages())
                .isLast(limitPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ChannelResponse getChannelLimitDetails(ChannelDetailsRequest request) {
        if (request == null) {
            throw new LimitException(LimitConstants.ERR_CHANNEL_REQUIRED);
        }
        log.info("Fetching channel limit details by id: {} or channel: {}", request.getId(), request.getChannel());

        ChannelLimit limit;
        if (request.getId() != null) {
            limit = channelLimitRepository.findById(request.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(LimitConstants.ERR_LIMIT_NOT_FOUND + request.getId()));
        } else if (request.getChannel() != null) {
            limit = channelLimitRepository.findByChannelAndStatus(request.getChannel(), LimitStatus.ACTIVE)
                    .orElseThrow(() -> new ResourceNotFoundException(LimitConstants.ERR_LIMIT_NOT_FOUND + request.getChannel()));
        } else {
            throw new LimitException(LimitConstants.ERR_CHANNEL_REQUIRED);
        }

        return toChannelLimitResponse(limit);
    }

    @Override
    @Transactional
    public ChannelResponse createChannelLimit(CreateChannelRequest request) {
        log.info("Creating channel limit for channel: {}", request.getChannel());

        if (channelLimitRepository.existsByChannelAndStatus(request.getChannel(), LimitStatus.ACTIVE)) {
            throw new LimitException(LimitConstants.ERR_LIMIT_ALREADY_EXISTS + request.getChannel());
        }

        ChannelLimit limit = ChannelLimit.builder()
                .channel(request.getChannel())
                .dailyLimit(request.getDailyLimit() != null ? request.getDailyLimit() : LimitConstants.DEFAULT_CHANNEL_DAILY_LIMIT)
                .monthlyLimit(request.getMonthlyLimit() != null ? request.getMonthlyLimit() : LimitConstants.DEFAULT_CHANNEL_MONTHLY_LIMIT)
                .perTransactionLimit(request.getPerTransactionLimit() != null ? request.getPerTransactionLimit() : LimitConstants.DEFAULT_CHANNEL_TXN_LIMIT)
                .maxTransactionsPerDay(request.getMaxTransactionsPerDay() != null ? request.getMaxTransactionsPerDay() : LimitConstants.DEFAULT_CHANNEL_MAX_TXN_PER_DAY)
                .currency(AppConstants.DEFAULT_CURRENCY)
                .status(LimitStatus.ACTIVE)
                .build();

        ChannelLimit saved = channelLimitRepository.save(limit);
        recordChannelHistory(saved, LimitAction.CREATE, "Initial channel limit configured");

        return toChannelLimitResponse(saved);
    }

    @Override
    @Transactional
    public ChannelResponse updateChannelLimit(UpdateChannelRequest request) {
        log.info("Updating channel limit ID: {} / channel: {}", request.getId(), request.getChannel());

        ChannelLimit limit;
        if (request.getId() != null) {
            limit = channelLimitRepository.findById(request.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(LimitConstants.ERR_LIMIT_NOT_FOUND + request.getId()));
        } else if (request.getChannel() != null) {
            limit = channelLimitRepository.findByChannelAndStatus(request.getChannel(), LimitStatus.ACTIVE)
                    .orElseThrow(() -> new ResourceNotFoundException(LimitConstants.ERR_LIMIT_NOT_FOUND + request.getChannel()));
        } else {
            throw new LimitException(LimitConstants.ERR_CHANNEL_REQUIRED);
        }

        if (request.getDailyLimit() != null) {
            limit.setDailyLimit(request.getDailyLimit());
        }
        if (request.getMonthlyLimit() != null) {
            limit.setMonthlyLimit(request.getMonthlyLimit());
        }
        if (request.getPerTransactionLimit() != null) {
            limit.setPerTransactionLimit(request.getPerTransactionLimit());
        }
        if (request.getMaxTransactionsPerDay() != null) {
            limit.setMaxTransactionsPerDay(request.getMaxTransactionsPerDay());
        }
        if (request.getStatus() != null) {
            limit.setStatus(request.getStatus());
        }

        ChannelLimit updated = channelLimitRepository.save(limit);
        recordChannelHistory(updated, LimitAction.UPDATE, "Updated channel limit policy");

        return toChannelLimitResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ChannelHistoryResponse> getChannelLimitHistory(ChannelHistoryRequest request) {
        int page = (request != null && request.getPage() != null) ? request.getPage() : AppConstants.DEFAULT_PAGE_NUMBER;
        int size = (request != null && request.getSize() != null) ? request.getSize() : AppConstants.DEFAULT_PAGE_SIZE;
        Pageable pageable = PageRequest.of(page, size);

        Page<ChannelLimitHistory> historyPage;
        if (request != null && request.getChannel() != null) {
            historyPage = channelLimitHistoryRepository.findByChannelOrderByTimestampDesc(request.getChannel(), pageable);
        } else {
            historyPage = channelLimitHistoryRepository.findAllByOrderByTimestampDesc(pageable);
        }

        List<ChannelHistoryResponse> content = historyPage.getContent().stream()
                .map(h -> ChannelHistoryResponse.builder()
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

        return PageResponse.<ChannelHistoryResponse>builder()
                .content(content)
                .pageNumber(historyPage.getNumber())
                .pageSize(historyPage.getSize())
                .totalElements(historyPage.getTotalElements())
                .totalPages(historyPage.getTotalPages())
                .isLast(historyPage.isLast())
                .build();
    }

    // ==========================================
    // Customer Limits
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> searchCustomerLimits(CustomerSearchRequest request) {
        int page = (request != null && request.getPage() != null) ? request.getPage() : AppConstants.DEFAULT_PAGE_NUMBER;
        int size = (request != null && request.getSize() != null) ? request.getSize() : AppConstants.DEFAULT_PAGE_SIZE;
        Pageable pageable = PageRequest.of(page, size);

        String customerId = (request != null && request.getCustomerId() != null && !request.getCustomerId().isBlank())
                ? request.getCustomerId()
                : keycloakAdapter.getCifFromCurrentToken().orElse(null);

        LimitChannel channel = request != null ? request.getChannel() : null;
        log.info("Searching customer limits for CIF: {}, channel: {}", customerId, channel);

        Page<CustomerLimit> limitPage;
        if (customerId != null && channel != null) {
            limitPage = customerLimitRepository.findByCustomerIdAndChannel(customerId, channel, pageable);
        } else if (customerId != null) {
            limitPage = customerLimitRepository.findByCustomerId(customerId, pageable);
        } else {
            limitPage = customerLimitRepository.findAll(pageable);
        }

        List<CustomerResponse> content = limitPage.getContent().stream()
                .map(this::toCustomerLimitResponse)
                .toList();

        return PageResponse.<CustomerResponse>builder()
                .content(content)
                .pageNumber(limitPage.getNumber())
                .pageSize(limitPage.getSize())
                .totalElements(limitPage.getTotalElements())
                .totalPages(limitPage.getTotalPages())
                .isLast(limitPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerLimitDetails(CustomerDetailsRequest request) {
        log.info("Fetching customer limit details");

        CustomerLimit limit;
        if (request != null && request.getId() != null) {
            limit = customerLimitRepository.findById(request.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(LimitConstants.ERR_LIMIT_NOT_FOUND + request.getId()));
        } else {
            String customerId = (request != null && request.getCustomerId() != null && !request.getCustomerId().isBlank())
                    ? request.getCustomerId()
                    : keycloakAdapter.resolveCurrentCif(null);
            LimitChannel channel = (request != null && request.getChannel() != null) ? request.getChannel() : LimitChannel.ALL;

            limit = customerLimitRepository.findByCustomerIdAndChannelAndStatus(customerId, channel, LimitStatus.ACTIVE)
                    .orElseThrow(() -> new ResourceNotFoundException(LimitConstants.ERR_LIMIT_NOT_FOUND + customerId));
        }

        return toCustomerLimitResponse(limit);
    }

    @Override
    @Transactional
    public CustomerResponse createCustomerLimit(CreateCustomerRequest request) {
        String customerId = (request.getCustomerId() != null && !request.getCustomerId().isBlank())
                ? request.getCustomerId()
                : keycloakAdapter.resolveCurrentCif(null);

        log.info("Creating customer limit for CIF: {}, channel: {}", customerId, request.getChannel());

        if (customerLimitRepository.existsByCustomerIdAndChannelAndStatus(customerId, request.getChannel(), LimitStatus.ACTIVE)) {
            throw new LimitException(LimitConstants.ERR_LIMIT_ALREADY_EXISTS + customerId + " / " + request.getChannel());
        }

        CustomerLimit limit = CustomerLimit.builder()
                .customerId(customerId)
                .channel(request.getChannel())
                .dailyLimit(request.getDailyLimit() != null ? request.getDailyLimit() : LimitConstants.DEFAULT_CUSTOMER_DAILY_LIMIT)
                .monthlyLimit(request.getMonthlyLimit() != null ? request.getMonthlyLimit() : LimitConstants.DEFAULT_CUSTOMER_MONTHLY_LIMIT)
                .perTransactionLimit(request.getPerTransactionLimit() != null ? request.getPerTransactionLimit() : LimitConstants.DEFAULT_CUSTOMER_TXN_LIMIT)
                .maxTransactionsPerDay(request.getMaxTransactionsPerDay() != null ? request.getMaxTransactionsPerDay() : LimitConstants.DEFAULT_CUSTOMER_MAX_TXN_PER_DAY)
                .currency(AppConstants.DEFAULT_CURRENCY)
                .status(LimitStatus.ACTIVE)
                .build();

        CustomerLimit saved = customerLimitRepository.save(limit);
        recordCustomerHistory(saved, LimitAction.CREATE, "Initial customer limit set");

        return toCustomerLimitResponse(saved);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomerLimit(UpdateCustomerRequest request) {
        log.info("Updating customer limit ID: {} / CIF: {}", request.getId(), request.getCustomerId());

        CustomerLimit limit;
        if (request.getId() != null) {
            limit = customerLimitRepository.findById(request.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(LimitConstants.ERR_LIMIT_NOT_FOUND + request.getId()));
        } else {
            String customerId = (request.getCustomerId() != null && !request.getCustomerId().isBlank())
                    ? request.getCustomerId()
                    : keycloakAdapter.resolveCurrentCif(null);
            LimitChannel channel = (request.getChannel() != null) ? request.getChannel() : LimitChannel.ALL;

            limit = customerLimitRepository.findByCustomerIdAndChannelAndStatus(customerId, channel, LimitStatus.ACTIVE)
                    .orElseThrow(() -> new ResourceNotFoundException(LimitConstants.ERR_LIMIT_NOT_FOUND + customerId));
        }

        if (request.getDailyLimit() != null) {
            limit.setDailyLimit(request.getDailyLimit());
        }
        if (request.getMonthlyLimit() != null) {
            limit.setMonthlyLimit(request.getMonthlyLimit());
        }
        if (request.getPerTransactionLimit() != null) {
            limit.setPerTransactionLimit(request.getPerTransactionLimit());
        }
        if (request.getMaxTransactionsPerDay() != null) {
            limit.setMaxTransactionsPerDay(request.getMaxTransactionsPerDay());
        }
        if (request.getStatus() != null) {
            limit.setStatus(request.getStatus());
        }

        CustomerLimit updated = customerLimitRepository.save(limit);
        recordCustomerHistory(updated, LimitAction.UPDATE, "Updated customer limit policy");

        return toCustomerLimitResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerHistoryResponse> getCustomerLimitHistory(CustomerHistoryRequest request) {
        int page = (request != null && request.getPage() != null) ? request.getPage() : AppConstants.DEFAULT_PAGE_NUMBER;
        int size = (request != null && request.getSize() != null) ? request.getSize() : AppConstants.DEFAULT_PAGE_SIZE;
        Pageable pageable = PageRequest.of(page, size);

        String customerId = (request != null && request.getCustomerId() != null && !request.getCustomerId().isBlank())
                ? request.getCustomerId()
                : keycloakAdapter.getCifFromCurrentToken().orElse(null);

        Page<CustomerLimitHistory> historyPage;
        if (customerId != null) {
            historyPage = customerLimitHistoryRepository.findByCustomerIdOrderByTimestampDesc(customerId, pageable);
        } else {
            historyPage = customerLimitHistoryRepository.findAllByOrderByTimestampDesc(pageable);
        }

        List<CustomerHistoryResponse> content = historyPage.getContent().stream()
                .map(h -> CustomerHistoryResponse.builder()
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

        return PageResponse.<CustomerHistoryResponse>builder()
                .content(content)
                .pageNumber(historyPage.getNumber())
                .pageSize(historyPage.getSize())
                .totalElements(historyPage.getTotalElements())
                .totalPages(historyPage.getTotalPages())
                .isLast(historyPage.isLast())
                .build();
    }

    // ==========================================
    // Global / Admin Limits
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GlobalResponse> searchGlobalLimits(GlobalSearchRequest request) {
        int page = (request != null && request.getPage() != null) ? request.getPage() : AppConstants.DEFAULT_PAGE_NUMBER;
        int size = (request != null && request.getSize() != null) ? request.getSize() : AppConstants.DEFAULT_PAGE_SIZE;
        Pageable pageable = PageRequest.of(page, size);

        String limitCode = request != null ? request.getLimitCode() : null;
        LimitStatus status = request != null ? request.getStatus() : null;
        log.info("Searching global limits for code: {}, status: {}", limitCode, status);

        Page<GlobalLimit> limitPage = globalLimitRepository.searchGlobalLimits(limitCode, status, pageable);

        List<GlobalResponse> content = limitPage.getContent().stream()
                .map(this::toGlobalLimitResponse)
                .toList();

        return PageResponse.<GlobalResponse>builder()
                .content(content)
                .pageNumber(limitPage.getNumber())
                .pageSize(limitPage.getSize())
                .totalElements(limitPage.getTotalElements())
                .totalPages(limitPage.getTotalPages())
                .isLast(limitPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GlobalResponse getGlobalLimitDetails(GlobalDetailsRequest request) {
        log.info("Fetching global limit details");

        GlobalLimit limit;
        if (request != null && request.getId() != null) {
            limit = globalLimitRepository.findById(request.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(LimitConstants.ERR_LIMIT_NOT_FOUND + request.getId()));
        } else if (request != null && request.getLimitCode() != null) {
            limit = globalLimitRepository.findByLimitCode(request.getLimitCode())
                    .orElseThrow(() -> new ResourceNotFoundException(LimitConstants.ERR_LIMIT_NOT_FOUND + request.getLimitCode()));
        } else {
            limit = globalLimitRepository.findByLimitCode(LimitConstants.GLOBAL_LIMIT_CODE_DEFAULT)
                    .orElseThrow(() -> new ResourceNotFoundException(LimitConstants.ERR_LIMIT_NOT_FOUND));
        }

        return toGlobalLimitResponse(limit);
    }

    @Override
    @Transactional
    public GlobalResponse createGlobalLimit(CreateGlobalRequest request) {
        log.info("Creating global limit policy: {}", request.getLimitCode());

        if (globalLimitRepository.existsByLimitCode(request.getLimitCode())) {
            throw new LimitException(LimitConstants.ERR_LIMIT_ALREADY_EXISTS + request.getLimitCode());
        }

        GlobalLimit limit = GlobalLimit.builder()
                .limitCode(request.getLimitCode())
                .limitName(request.getLimitName() != null ? request.getLimitName() : LimitConstants.GLOBAL_LIMIT_NAME_DEFAULT)
                .dailyLimit(request.getDailyLimit() != null ? request.getDailyLimit() : LimitConstants.DEFAULT_GLOBAL_DAILY_LIMIT)
                .monthlyLimit(request.getMonthlyLimit() != null ? request.getMonthlyLimit() : LimitConstants.DEFAULT_GLOBAL_MONTHLY_LIMIT)
                .perTransactionLimit(request.getPerTransactionLimit() != null ? request.getPerTransactionLimit() : LimitConstants.DEFAULT_GLOBAL_TXN_LIMIT)
                .coolingPeriodHours(request.getCoolingPeriodHours() != null ? request.getCoolingPeriodHours() : LimitConstants.DEFAULT_COOLING_PERIOD_HOURS)
                .description(request.getDescription())
                .currency(AppConstants.DEFAULT_CURRENCY)
                .status(LimitStatus.ACTIVE)
                .build();

        GlobalLimit saved = globalLimitRepository.save(limit);
        recordGlobalHistory(saved, LimitAction.CREATE, "Initial global limit policy created");

        return toGlobalLimitResponse(saved);
    }

    @Override
    @Transactional
    public GlobalResponse updateGlobalLimit(UpdateGlobalRequest request) {
        log.info("Updating global limit ID: {} / code: {}", request.getId(), request.getLimitCode());

        GlobalLimit limit;
        if (request.getId() != null) {
            limit = globalLimitRepository.findById(request.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(LimitConstants.ERR_LIMIT_NOT_FOUND + request.getId()));
        } else if (request.getLimitCode() != null) {
            limit = globalLimitRepository.findByLimitCode(request.getLimitCode())
                    .orElseThrow(() -> new ResourceNotFoundException(LimitConstants.ERR_LIMIT_NOT_FOUND + request.getLimitCode()));
        } else {
            throw new LimitException(LimitConstants.ERR_GLOBAL_CODE_REQUIRED);
        }

        if (request.getLimitName() != null) {
            limit.setLimitName(request.getLimitName());
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
    public PageResponse<GlobalHistoryResponse> getGlobalLimitHistory(GlobalHistoryRequest request) {
        int page = (request != null && request.getPage() != null) ? request.getPage() : AppConstants.DEFAULT_PAGE_NUMBER;
        int size = (request != null && request.getSize() != null) ? request.getSize() : AppConstants.DEFAULT_PAGE_SIZE;
        Pageable pageable = PageRequest.of(page, size);

        Page<GlobalLimitHistory> historyPage;
        if (request != null && request.getLimitCode() != null && !request.getLimitCode().isBlank()) {
            historyPage = globalLimitHistoryRepository.findByLimitCodeOrderByTimestampDesc(request.getLimitCode(), pageable);
        } else {
            historyPage = globalLimitHistoryRepository.findAllByOrderByTimestampDesc(pageable);
        }

        List<GlobalHistoryResponse> content = historyPage.getContent().stream()
                .map(h -> GlobalHistoryResponse.builder()
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

        return PageResponse.<GlobalHistoryResponse>builder()
                .content(content)
                .pageNumber(historyPage.getNumber())
                .pageSize(historyPage.getSize())
                .totalElements(historyPage.getTotalElements())
                .totalPages(historyPage.getTotalPages())
                .isLast(historyPage.isLast())
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

    private ChannelResponse toChannelLimitResponse(ChannelLimit l) {
        return ChannelResponse.builder()
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

    private CustomerResponse toCustomerLimitResponse(CustomerLimit l) {
        return CustomerResponse.builder()
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

    private GlobalResponse toGlobalLimitResponse(GlobalLimit l) {
        return GlobalResponse.builder()
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
