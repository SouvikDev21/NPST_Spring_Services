package com.term_deposit.backend;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cloud.openfeign.EnableFeignClients;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@EnableFeignClients // CRITICAL: This tells Spring to load your CbsFeignClient
public class TermDepositServiceApplication {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
    }

    public static void main(String[] args) {
        SpringApplication.run(TermDepositServiceApplication.class, args);
    }
}