package com.fund_transfer.backend.dto.request.limit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalLimitHistoryRequest {
    private String limitCode;
    private Integer page;
    private Integer size;
}
