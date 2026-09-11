package com.common.cbs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "cbs")
public class CbsProperties {
    private String baseUrl;
    private String channelId;
    private String userId;
    private String branchCode;
    private Integer connectTimeoutMs;
    private Integer readTimeoutMs;
}
