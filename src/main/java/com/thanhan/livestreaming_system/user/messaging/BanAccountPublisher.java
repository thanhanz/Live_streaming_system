package com.thanhan.livestreaming_system.user.messaging;
import com.thanhan.livestreaming_system.user.dto.request.BanAccountEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class BanAccountPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.email.exchange}")
    private String emailExchange;

    @Value("${rabbitmq.email.routing-key}")
    private String emailRoutingKey;
    public void sendEmailMessage(String userId, String to, String subject, String body, String action) {
        BanAccountEvent banAccountEvent = new BanAccountEvent(userId ,to, subject, body, action);
        rabbitTemplate.convertAndSend(emailExchange, emailRoutingKey, banAccountEvent);
    }
}
