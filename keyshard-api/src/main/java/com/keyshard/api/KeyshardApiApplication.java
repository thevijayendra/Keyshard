package com.keyshard.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.keyshard")
public class KeyshardApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(KeyshardApiApplication.class, args);
	}

}
