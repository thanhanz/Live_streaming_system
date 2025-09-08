package com.thanhan.livestreaming_system.livestream.messaging;

import com.thanhan.livestreaming_system.livestream.dto.mapper.StreamMapper;
import com.thanhan.livestreaming_system.livestream.dto.response.StreamSearchDocument;
import com.thanhan.livestreaming_system.livestream.entity.Stream;
import com.thanhan.livestreaming_system.search_service.dto.SearchEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreamTranscodeProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.transcode}")
    private String transcodeExchange;

    @Value("${rabbitmq.transcode.live.routing-key}")
    private String transcodeRoutingKey;

    @Value("${rabbitmq.exchange.search}")
    private String searchExchange;

    private String prefixSearchRoutingKey = "event.search.live.";

    public void sendMessage(String streamKey) throws IOException {
        rabbitTemplate.convertAndSend(transcodeExchange, transcodeRoutingKey, streamKey);
    }

    public void sendToSearchConsumer(Stream stream, String status) {
        String action = status.equals("streaming") ? "create"  : "delete";
        StreamSearchDocument doc = StreamMapper.toStreamSearchDocument(stream);

        Map<String, Object> payload = new HashMap<>();
        payload.put("title", doc.title());
        payload.put("thumbnailUrl", doc.thumbnailUrl());
        payload.put("channelId", doc.channelId());
        payload.put("channelName", doc.channelName());
        payload.put("avatarUrl", doc.avatarUrl());

        SearchEvent event = new SearchEvent("live", stream.getId().toString(), action, payload);

        String routingKey = prefixSearchRoutingKey + action;
        rabbitTemplate.convertAndSend(searchExchange, routingKey, event);
        log.info("Send event: [{}]", routingKey);
    }

}
