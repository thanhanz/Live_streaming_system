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
    private Exchanges exchange;
    private Transcode transcode;
    private Search search;
    private SendEmail email;

    @Getter @Setter
    public static class Exchanges {
        private String transcode;
        private String search;
    }

    @Getter @Setter
    public static class Transcode {
        private Queues live;
        private Queues vod;
    }

    @Getter @Setter
    public static class Search {
        private String queue;
        private String routingKey;
    }

    @Getter @Setter
    public static class Queues {
        private String queue;
        private String routingKey;
    }

    @Getter @Setter
    public static class SendEmail {
        private String exchange;
        private String queue;
        private String routingKey;
    }
}
