package com.term_deposit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients // CRITICAL: This tells Spring to load your CbsFeignClient
public class TermDepositServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TermDepositServiceApplication.class, args);
    }
}