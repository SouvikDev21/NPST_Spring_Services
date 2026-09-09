package com.fund_transfer.backend.dto.request.limit;

import com.fund_transfer.backend.enums.LimitChannel;
import com.fund_transfer.backend.enums.LimitStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelLimitSearchRequest {
    private LimitChannel channel;
    private LimitStatus status;
    private Integer page;
    private Integer size;
}
