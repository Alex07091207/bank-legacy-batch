package com.banco.bank_legacy_batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient 
public class BankLegacyBatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankLegacyBatchApplication.class, args);
	}

}
