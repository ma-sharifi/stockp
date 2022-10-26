package com.example.stockp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class StockpApplication {

	public static void main(String[] args) {
		if(log.isDebugEnabled()) log.warn("##IMPORTANT NOTE: Debug mode is activated! You/Customer can see the details of your errors!##");
		SpringApplication.run(StockpApplication.class, args);
	}

}
