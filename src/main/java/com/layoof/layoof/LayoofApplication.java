package com.layoof.layoof;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LayoofApplication {

	public static void main(String[] args) {
		SpringApplication.run(LayoofApplication.class, args);
	}

}
