package com.thanhan.livestreaming_system.livestream.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class StreamTranscodeProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.transcode.vod.routing-key}")
    private String routingKey;

    public void sendMessage(String streamKey) throws IOException {
        rabbitTemplate.convertAndSend(exchange, routingKey, streamKey);
    }

}
