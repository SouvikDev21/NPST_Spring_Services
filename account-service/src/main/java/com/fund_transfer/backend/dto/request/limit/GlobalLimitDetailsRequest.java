package com.fund_transfer.backend.dto.request.limit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalLimitDetailsRequest {
    private UUID limitId;
    private String limitCode;
}
