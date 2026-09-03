package com.layoof.layoof;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableWebSecurity
public class LayoofApplication {

	public static void main(String[] args) {
		SpringApplication.run(LayoofApplication.class, args);
	}

}
