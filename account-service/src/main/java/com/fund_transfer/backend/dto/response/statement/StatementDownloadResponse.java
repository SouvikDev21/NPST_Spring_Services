package com.fund_transfer.backend.dto.response.statement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fund_transfer.backend.common.constants.AppConstants;
import com.fund_transfer.backend.enums.StatementFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatementDownloadResponse {
    private String accountNumber;

    @JsonFormat(pattern = AppConstants.ISO_DATE_FORMAT)
    private LocalDate fromDate;

    @JsonFormat(pattern = AppConstants.ISO_DATE_FORMAT)
    private LocalDate toDate;

    private StatementFormat format;
    private String fileName;
    private String contentType;
    private String fileContentBase64;
    private long fileSizeBytes;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = AppConstants.ISO_DATETIME_FORMAT, timezone = AppConstants.UTC_ZONE)
    private Instant generatedAt;
}
