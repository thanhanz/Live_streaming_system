package com.thanhan.livestreaming_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LivestreamingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(LivestreamingSystemApplication.class, args);
	}

}
