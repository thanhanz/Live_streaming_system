package com.thanhan.livestreaming_system.user.messaging;


import com.thanhan.livestreaming_system.search_service.dto.SearchEvent;
import com.thanhan.livestreaming_system.user.dto.mapper.ChannelMapper;
import com.thanhan.livestreaming_system.user.dto.response.ChannelToDocumentSearch;
import com.thanhan.livestreaming_system.user.entity.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Slf4j
public class ChannelEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.search}")
    private String searchExchange;

    private String prefixSearchChannelRoutingKey = "event.search.channel.";

    public void sendMessage(Channel channel, String action) {
        ChannelToDocumentSearch doc = ChannelMapper.toDocumentSearch(channel);
        Map<String, Object> payload = new HashMap<>();

        if (!action.equals("delete")) {
            payload.put("entityId", channel.getId().toString());
            payload.put("channelName", doc.channelName());
            payload.put("channelAvatar", doc.channelAvatar());
            payload.put("channelFollowers", doc.channelFollowers());
        }
        //Create SearchEvent
        SearchEvent event = new SearchEvent("channel", channel.getId().toString(), action, payload);

        //Send routing-key suitable for action
        String routingKey = prefixSearchChannelRoutingKey + action;
        rabbitTemplate.convertAndSend(searchExchange, routingKey, event);
        log.info("Send event from vod publisher: [{}]", routingKey);
    }
}
