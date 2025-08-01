package com.thanhan.livestreaming_system.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    public static final String TRANSCODE_QUEUE = "video.transcode.queue";
    public static final String TRANSCODE_EXCHANGE = "video.transcode.exchange";
    public static final String TRANSCODE_ROUTING_KEY = "video.transcode";


    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    @Bean
    public Queue queue() {
        return new Queue(TRANSCODE_QUEUE,  true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(TRANSCODE_EXCHANGE);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {

        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(TRANSCODE_ROUTING_KEY);
    }
}

