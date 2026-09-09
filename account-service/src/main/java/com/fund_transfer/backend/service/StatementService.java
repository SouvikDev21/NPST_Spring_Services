package com.fund_transfer.backend.service;

import com.fund_transfer.backend.dto.request.statement.StatementDownloadRequest;
import com.fund_transfer.backend.dto.request.statement.StatementEmailRequest;
import com.fund_transfer.backend.dto.request.statement.StatementSearchRequest;
import com.fund_transfer.backend.dto.response.statement.StatementDownloadResponse;
import com.fund_transfer.backend.dto.response.statement.StatementEmailResponse;
import com.fund_transfer.backend.dto.response.statement.StatementSearchResponse;

public interface StatementService {

    StatementSearchResponse searchStatements(StatementSearchRequest request);

    StatementDownloadResponse downloadStatement(StatementDownloadRequest request);

    StatementEmailResponse emailStatement(StatementEmailRequest request);
}
