package com.bank.pe.mscredit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
@EnableReactiveMongoRepositories(basePackages = "com.bank.pe.mscredit.repository")
public class MsCreditsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsCreditsApplication.class, args);
	}

}
