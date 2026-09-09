package com.fund_transfer.backend.dto.request.limit;

import com.fund_transfer.backend.enums.LimitChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerLimitHistoryRequest {
    private String customerId;
    private LimitChannel channel;
    private Integer page;
    private Integer size;
}
