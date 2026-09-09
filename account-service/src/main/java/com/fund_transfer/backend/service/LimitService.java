package com.fund_transfer.backend.service;

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

public interface LimitService {

    // Channel Limits
    PageResponse<ChannelLimitResponse> searchChannelLimits(ChannelLimitSearchRequest request);

    ChannelLimitResponse getChannelLimitDetails(ChannelLimitDetailsRequest request);

    ChannelLimitResponse createChannelLimit(CreateChannelLimitRequest request);

    ChannelLimitResponse updateChannelLimit(UpdateChannelLimitRequest request);

    PageResponse<ChannelLimitHistoryResponse> getChannelLimitHistory(ChannelLimitHistoryRequest request);

    // Customer Limits
    PageResponse<CustomerLimitResponse> searchCustomerLimits(CustomerLimitSearchRequest request);

    CustomerLimitResponse getCustomerLimitDetails(CustomerLimitDetailsRequest request);

    CustomerLimitResponse createCustomerLimit(CreateCustomerLimitRequest request);

    CustomerLimitResponse updateCustomerLimit(UpdateCustomerLimitRequest request);

    PageResponse<CustomerLimitHistoryResponse> getCustomerLimitHistory(CustomerLimitHistoryRequest request);

    // Global / Admin Limits
    PageResponse<GlobalLimitResponse> searchGlobalLimits(GlobalLimitSearchRequest request);

    GlobalLimitResponse getGlobalLimitDetails(GlobalLimitDetailsRequest request);

    GlobalLimitResponse createGlobalLimit(CreateGlobalLimitRequest request);

    GlobalLimitResponse updateGlobalLimit(UpdateGlobalLimitRequest request);

    PageResponse<GlobalLimitHistoryResponse> getGlobalLimitHistory(GlobalLimitHistoryRequest request);
}
