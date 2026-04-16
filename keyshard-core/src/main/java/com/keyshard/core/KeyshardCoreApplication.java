package com.keyshard.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

//@ConfigurationPropertiesScan("com.keyshard.core")

@SpringBootApplication
public class KeyshardCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(KeyshardCoreApplication.class, args);
	}

}
