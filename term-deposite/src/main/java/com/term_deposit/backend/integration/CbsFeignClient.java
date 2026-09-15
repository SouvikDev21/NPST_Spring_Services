package com.term_deposit.backend.integration;

import com.term_deposit.backend.dto.Request.CbsOpenTdRequest;
import com.term_deposit.backend.dto.Response.CbsOpenTdResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "cbsFeignClient", url = "http://localhost:8080/mock")
public interface CbsFeignClient {

    @PostMapping("/api/v3/deposits/td/open")
    CbsOpenTdResponse openTermDeposit(@RequestBody CbsOpenTdRequest request);

}