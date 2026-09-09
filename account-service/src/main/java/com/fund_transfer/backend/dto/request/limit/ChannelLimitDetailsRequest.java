package com.fund_transfer.backend.dto.request.limit;

import com.fund_transfer.backend.enums.LimitChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelLimitDetailsRequest {
    private UUID limitId;
    private LimitChannel channel;
}
