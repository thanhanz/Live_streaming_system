package com.thanhan.livestreaming_system.user.messaging;

import com.thanhan.livestreaming_system.user.dto.request.BanAccountEvent;
import com.thanhan.livestreaming_system.user.entity.Channel;
import com.thanhan.livestreaming_system.user.service.ChannelService;
import com.thanhan.livestreaming_system.user.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class BanAccountConsumer {

    private final EmailService emailService;
    private final ChannelService channelService;
    @RabbitListener(queues = "${rabbitmq.email.queue}")
    public void sendEmail(BanAccountEvent banAccountEvent) {
        if (banAccountEvent.action().equalsIgnoreCase("ban")) {
            emailService.sendEmail(banAccountEvent.email(), banAccountEvent.subject(), banAccountEvent.body());
            log.info("[Send email] to: " + banAccountEvent.email());
        }

        Channel channel = channelService.getChannelByOwnerId(banAccountEvent.userId());
        if (channel != null) {
            channelService.banOrUnbanChannel(channel.getId());
            log.info("[Ban channel] : " + channel.getDisplayName());
        }
    }
}
