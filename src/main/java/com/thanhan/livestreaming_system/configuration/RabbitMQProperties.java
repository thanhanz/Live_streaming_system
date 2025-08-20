package com.thanhan.livestreaming_system.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitMQProperties {
    private String exchange;
    private Transcode transcode;

    @Getter @Setter
    public static class Transcode {
        private QueueProps live;
        private QueueProps vod;
    }

    @Getter @Setter
    public static class QueueProps {
        private String queue;
        private String routingKey;
    }
}
