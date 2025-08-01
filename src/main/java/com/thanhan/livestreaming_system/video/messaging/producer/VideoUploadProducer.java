package com.thanhan.livestreaming_system.video.messaging.producer;


import com.thanhan.livestreaming_system.video.dto.VodTranscodeRequest;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class VideoUploadProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.transcode.exchange}")
    private String exchange;

    @Value("${rabbitmq.transcode.routing-key}")
    private String routingKey;

    public void sendMessage(VodTranscodeRequest request) {
        rabbitTemplate.convertAndSend(exchange, routingKey, request);
    }
}
