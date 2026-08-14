package com.intern.fwork;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.cache.annotation.EnableCaching
@org.springframework.scheduling.annotation.EnableAsync
public class FworkApplication {

	public static void main(String[] args) {
		SpringApplication.run(FworkApplication.class, args);
	}

}
