package com.account_service.backend.controller;

import com.account_service.backend.dto.common.ApiResponse;
import com.account_service.backend.dto.common.PageResponse;
import com.account_service.backend.dto.limit.LimitDto.*;
import com.account_service.backend.service.LimitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/limits")
@RequiredArgsConstructor
public class LimitController {

    private final LimitService limitService;

    // ==========================================
    // Channel Limits
    // ==========================================

    @PostMapping("/channel/search")
    public ResponseEntity<ApiResponse<PageResponse<ChannelResponse>>> searchChannelLimits(
            @RequestBody(required = false) ChannelSearchRequest request
    ) {
        log.info("REST request to search channel limits");
        PageResponse<ChannelResponse> response = limitService.searchChannelLimits(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/channel/details")
    public ResponseEntity<ApiResponse<ChannelResponse>> getChannelLimitDetails(
            @RequestBody(required = false) ChannelDetailsRequest request
    ) {
        log.info("REST request to get channel limit details");
        ChannelResponse response = limitService.getChannelLimitDetails(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/channel/create")
    public ResponseEntity<ApiResponse<ChannelResponse>> createChannelLimit(
            @Valid @RequestBody CreateChannelRequest request
    ) {
        log.info("REST request to create channel limit for: {}", request.getChannel());
        ChannelResponse response = limitService.createChannelLimit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PostMapping("/channel/update")
    public ResponseEntity<ApiResponse<ChannelResponse>> updateChannelLimit(
            @Valid @RequestBody UpdateChannelRequest request
    ) {
        log.info("REST request to update channel limit");
        ChannelResponse response = limitService.updateChannelLimit(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/channel/history")
    public ResponseEntity<ApiResponse<PageResponse<ChannelHistoryResponse>>> getChannelLimitHistory(
            @RequestBody(required = false) ChannelHistoryRequest request
    ) {
        log.info("REST request to get channel limit history");
        PageResponse<ChannelHistoryResponse> response = limitService.getChannelLimitHistory(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ==========================================
    // Customer Limits
    // ==========================================

    @PostMapping("/customer/search")
    public ResponseEntity<ApiResponse<PageResponse<CustomerResponse>>> searchCustomerLimits(
            @RequestBody(required = false) CustomerSearchRequest request
    ) {
        log.info("REST request to search customer limits");
        PageResponse<CustomerResponse> response = limitService.searchCustomerLimits(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/customer/details")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerLimitDetails(
            @RequestBody(required = false) CustomerDetailsRequest request
    ) {
        log.info("REST request to get customer limit details");
        CustomerResponse response = limitService.getCustomerLimitDetails(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/customer/create")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomerLimit(
            @Valid @RequestBody CreateCustomerRequest request
    ) {
        log.info("REST request to create customer limit for channel: {}", request.getChannel());
        CustomerResponse response = limitService.createCustomerLimit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PostMapping("/customer/update")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomerLimit(
            @Valid @RequestBody UpdateCustomerRequest request
    ) {
        log.info("REST request to update customer limit");
        CustomerResponse response = limitService.updateCustomerLimit(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/customer/history")
    public ResponseEntity<ApiResponse<PageResponse<CustomerHistoryResponse>>> getCustomerLimitHistory(
            @RequestBody(required = false) CustomerHistoryRequest request
    ) {
        log.info("REST request to get customer limit history");
        PageResponse<CustomerHistoryResponse> response = limitService.getCustomerLimitHistory(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ==========================================
    // Global / Admin Limits
    // ==========================================

    @PostMapping("/global/search")
    public ResponseEntity<ApiResponse<PageResponse<GlobalResponse>>> searchGlobalLimits(
            @RequestBody(required = false) GlobalSearchRequest request
    ) {
        log.info("REST request to search global limits");
        PageResponse<GlobalResponse> response = limitService.searchGlobalLimits(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/global/details")
    public ResponseEntity<ApiResponse<GlobalResponse>> getGlobalLimitDetails(
            @RequestBody(required = false) GlobalDetailsRequest request
    ) {
        log.info("REST request to get global limit details");
        GlobalResponse response = limitService.getGlobalLimitDetails(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/global/create")
    public ResponseEntity<ApiResponse<GlobalResponse>> createGlobalLimit(
            @Valid @RequestBody CreateGlobalRequest request
    ) {
        log.info("REST request to create global limit policy: {}", request.getLimitCode());
        GlobalResponse response = limitService.createGlobalLimit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PostMapping("/global/update")
    public ResponseEntity<ApiResponse<GlobalResponse>> updateGlobalLimit(
            @Valid @RequestBody UpdateGlobalRequest request
    ) {
        log.info("REST request to update global limit policy");
        GlobalResponse response = limitService.updateGlobalLimit(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/global/history")
    public ResponseEntity<ApiResponse<PageResponse<GlobalHistoryResponse>>> getGlobalLimitHistory(
            @RequestBody(required = false) GlobalHistoryRequest request
    ) {
        log.info("REST request to get global limit history");
        PageResponse<GlobalHistoryResponse> response = limitService.getGlobalLimitHistory(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
