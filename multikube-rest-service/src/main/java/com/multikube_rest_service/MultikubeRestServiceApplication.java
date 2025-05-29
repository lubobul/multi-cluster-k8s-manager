package com.multikube_rest_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; // Import this

@SpringBootApplication
@EnableScheduling // Add this annotation to enable scheduling
public class MultikubeRestServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MultikubeRestServiceApplication.class, args);
	}

}