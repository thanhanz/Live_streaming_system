package com.thanhan.livestreaming_system.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {

    private final RabbitMQProperties properties;

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
    public TopicExchange exchange() {
        return new TopicExchange(properties.getExchange());
    }

    @Bean
    public Queue liveQueue() {
        return new Queue(properties.getTranscode().getLive().getQueue(),  true);
    }

    @Bean
    public Binding liveBinding(Queue liveQueue, TopicExchange exchange) {
        return BindingBuilder
                .bind(liveQueue)
                .to(exchange)
                .with(properties.getTranscode().getLive().getRoutingKey());
    }

    @Bean
    public Queue vodQueue() {
        return new Queue(properties.getTranscode().getVod().getQueue(),  true);
    }

    @Bean
    public Binding vodBinding(Queue vodQueue, TopicExchange exchange) {
        return BindingBuilder
                .bind(vodQueue)
                .to(exchange)
                .with(properties.getTranscode().getVod().getRoutingKey());
    }


}

