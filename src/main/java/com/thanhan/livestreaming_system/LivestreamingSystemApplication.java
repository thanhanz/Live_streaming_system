package com.thanhan.livestreaming_system;

import com.thanhan.livestreaming_system.configuration.RabbitMQProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class LivestreamingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(LivestreamingSystemApplication.class, args);
	}

}
