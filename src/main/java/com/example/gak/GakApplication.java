package com.example.gak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class GakApplication {

	public static void main(String[] args) {
		SpringApplication.run(GakApplication.class, args);
	}

}
