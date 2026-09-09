package com.fund_transfer.backend.controller;

import com.fund_transfer.backend.dto.common.ApiResponse;
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
import com.fund_transfer.backend.service.LimitService;
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
    public ResponseEntity<ApiResponse<PageResponse<ChannelLimitResponse>>> searchChannelLimits(
            @RequestBody(required = false) ChannelLimitSearchRequest request
    ) {
        log.info("REST request to search channel limits");
        PageResponse<ChannelLimitResponse> response = limitService.searchChannelLimits(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/channel/details")
    public ResponseEntity<ApiResponse<ChannelLimitResponse>> getChannelLimitDetails(
            @RequestBody(required = false) ChannelLimitDetailsRequest request
    ) {
        log.info("REST request to get channel limit details");
        ChannelLimitResponse response = limitService.getChannelLimitDetails(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/channel/create")
    public ResponseEntity<ApiResponse<ChannelLimitResponse>> createChannelLimit(
            @Valid @RequestBody CreateChannelLimitRequest request
    ) {
        log.info("REST request to create channel limit for: {}", request.getChannel());
        ChannelLimitResponse response = limitService.createChannelLimit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PostMapping("/channel/update")
    public ResponseEntity<ApiResponse<ChannelLimitResponse>> updateChannelLimit(
            @Valid @RequestBody UpdateChannelLimitRequest request
    ) {
        log.info("REST request to update channel limit");
        ChannelLimitResponse response = limitService.updateChannelLimit(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/channel/history")
    public ResponseEntity<ApiResponse<PageResponse<ChannelLimitHistoryResponse>>> getChannelLimitHistory(
            @RequestBody(required = false) ChannelLimitHistoryRequest request
    ) {
        log.info("REST request to get channel limit history");
        PageResponse<ChannelLimitHistoryResponse> response = limitService.getChannelLimitHistory(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ==========================================
    // Customer Limits
    // ==========================================

    @PostMapping("/customer/search")
    public ResponseEntity<ApiResponse<PageResponse<CustomerLimitResponse>>> searchCustomerLimits(
            @RequestBody(required = false) CustomerLimitSearchRequest request
    ) {
        log.info("REST request to search customer limits");
        PageResponse<CustomerLimitResponse> response = limitService.searchCustomerLimits(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/customer/details")
    public ResponseEntity<ApiResponse<CustomerLimitResponse>> getCustomerLimitDetails(
            @RequestBody(required = false) CustomerLimitDetailsRequest request
    ) {
        log.info("REST request to get customer limit details");
        CustomerLimitResponse response = limitService.getCustomerLimitDetails(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/customer/create")
    public ResponseEntity<ApiResponse<CustomerLimitResponse>> createCustomerLimit(
            @Valid @RequestBody CreateCustomerLimitRequest request
    ) {
        log.info("REST request to create customer limit for channel: {}", request.getChannel());
        CustomerLimitResponse response = limitService.createCustomerLimit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PostMapping("/customer/update")
    public ResponseEntity<ApiResponse<CustomerLimitResponse>> updateCustomerLimit(
            @Valid @RequestBody UpdateCustomerLimitRequest request
    ) {
        log.info("REST request to update customer limit");
        CustomerLimitResponse response = limitService.updateCustomerLimit(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/customer/history")
    public ResponseEntity<ApiResponse<PageResponse<CustomerLimitHistoryResponse>>> getCustomerLimitHistory(
            @RequestBody(required = false) CustomerLimitHistoryRequest request
    ) {
        log.info("REST request to get customer limit history");
        PageResponse<CustomerLimitHistoryResponse> response = limitService.getCustomerLimitHistory(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ==========================================
    // Global / Admin Limits
    // ==========================================

    @PostMapping("/global/search")
    public ResponseEntity<ApiResponse<PageResponse<GlobalLimitResponse>>> searchGlobalLimits(
            @RequestBody(required = false) GlobalLimitSearchRequest request
    ) {
        log.info("REST request to search global limits");
        PageResponse<GlobalLimitResponse> response = limitService.searchGlobalLimits(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/global/details")
    public ResponseEntity<ApiResponse<GlobalLimitResponse>> getGlobalLimitDetails(
            @RequestBody(required = false) GlobalLimitDetailsRequest request
    ) {
        log.info("REST request to get global limit details");
        GlobalLimitResponse response = limitService.getGlobalLimitDetails(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/global/create")
    public ResponseEntity<ApiResponse<GlobalLimitResponse>> createGlobalLimit(
            @Valid @RequestBody CreateGlobalLimitRequest request
    ) {
        log.info("REST request to create global limit policy: {}", request.getLimitCode());
        GlobalLimitResponse response = limitService.createGlobalLimit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PostMapping("/global/update")
    public ResponseEntity<ApiResponse<GlobalLimitResponse>> updateGlobalLimit(
            @Valid @RequestBody UpdateGlobalLimitRequest request
    ) {
        log.info("REST request to update global limit policy");
        GlobalLimitResponse response = limitService.updateGlobalLimit(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/global/history")
    public ResponseEntity<ApiResponse<PageResponse<GlobalLimitHistoryResponse>>> getGlobalLimitHistory(
            @RequestBody(required = false) GlobalLimitHistoryRequest request
    ) {
        log.info("REST request to get global limit history");
        PageResponse<GlobalLimitHistoryResponse> response = limitService.getGlobalLimitHistory(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
