package com.account_service.backend.service;

import com.account_service.backend.dto.statement.StatementDto.*;

public interface StatementService {

    SearchResponse searchStatements(SearchRequest request);

    DownloadResponse downloadStatement(DownloadRequest request);

    EmailResponse emailStatement(EmailRequest request);
}
