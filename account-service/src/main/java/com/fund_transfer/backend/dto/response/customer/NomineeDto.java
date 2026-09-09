package com.fund_transfer.backend.dto.response.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NomineeDto {
    private String name;
    private String relation;
    private int sharePercentage;
    private boolean minor;
    private String guardianName;
}
