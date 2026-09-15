package com.term_deposit.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableScheduling
@EnableFeignClients // CRITICAL: This tells Spring to load your CbsFeignClient
public class TermDepositServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TermDepositServiceApplication.class, args);
    }
}