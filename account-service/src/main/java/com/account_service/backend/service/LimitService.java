package com.account_service.backend.service;

import com.account_service.backend.dto.common.PageResponse;
import com.account_service.backend.dto.limit.LimitDto.*;

public interface LimitService {

    // Channel Limits
    PageResponse<ChannelResponse> searchChannelLimits(ChannelSearchRequest request);

    ChannelResponse getChannelLimitDetails(ChannelDetailsRequest request);

    ChannelResponse createChannelLimit(CreateChannelRequest request);

    ChannelResponse updateChannelLimit(UpdateChannelRequest request);

    PageResponse<ChannelHistoryResponse> getChannelLimitHistory(ChannelHistoryRequest request);

    // Customer Limits
    PageResponse<CustomerResponse> searchCustomerLimits(CustomerSearchRequest request);

    CustomerResponse getCustomerLimitDetails(CustomerDetailsRequest request);

    CustomerResponse createCustomerLimit(CreateCustomerRequest request);

    CustomerResponse updateCustomerLimit(UpdateCustomerRequest request);

    PageResponse<CustomerHistoryResponse> getCustomerLimitHistory(CustomerHistoryRequest request);

    // Global / Admin Limits
    PageResponse<GlobalResponse> searchGlobalLimits(GlobalSearchRequest request);

    GlobalResponse getGlobalLimitDetails(GlobalDetailsRequest request);

    GlobalResponse createGlobalLimit(CreateGlobalRequest request);

    GlobalResponse updateGlobalLimit(UpdateGlobalRequest request);

    PageResponse<GlobalHistoryResponse> getGlobalLimitHistory(GlobalHistoryRequest request);
}
