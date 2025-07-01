package com.deardream.deardream_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DeardreamBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeardreamBeApplication.class, args);
	}

}
