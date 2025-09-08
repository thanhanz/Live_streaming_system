package com.thanhan.livestreaming_system.video.messaging.producer;


import com.thanhan.livestreaming_system.livestream.dto.response.StreamSearchDocument;
import com.thanhan.livestreaming_system.search_service.dto.SearchEvent;
import com.thanhan.livestreaming_system.video.dto.VodMapper;
import com.thanhan.livestreaming_system.video.dto.VodToDocumentSearch;
import com.thanhan.livestreaming_system.video.dto.VodTranscodeRequest;
import com.thanhan.livestreaming_system.video.entity.Vod;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
@Service
@Slf4j
public class VideoUploadProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.transcode}")
    private String vodTranscodeExchange;

    @Value("${rabbitmq.transcode.vod.routing-key}")
    private String vodTranscodeRoutingKey;

    @Value("${rabbitmq.exchange.search}")
    private String vodSearchExchange;

    private String prefixSearchRoutingKey = "event.search.vod.";

    public void sendMessage(VodTranscodeRequest request) {
        rabbitTemplate.convertAndSend(vodTranscodeExchange, vodTranscodeRoutingKey, request);
    }

    public void sendMessageToUpdateSearchService(Vod vod, String action) {
        //Mapper vod to Document
        VodToDocumentSearch doc = VodMapper.toDocumentSearch(vod);
        Map<String, Object> payload = new HashMap<>();

        if (!action.equals("delete")) {
            payload.put("entityId", vod.getId().toString());
            payload.put("title", doc.title());
            payload.put("description", doc.description());
            payload.put("thumbnailUrl", doc.thumbnailUrl());
            payload.put("isOnlyMember", doc.isOnlyMember());
            payload.put("viewCount", doc.viewCount());

            DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
            String createdAt = formatter.format(doc.createdAt());

            payload.put("createdAt", createdAt);
            payload.put("channelId", doc.channelId());
            payload.put("channelName", doc.channelName());
            payload.put("channelAvatar", doc.channelAvatar());
            payload.put("channelFollowers", doc.channelFollowers());
        }
        //Create SearchEvent
        SearchEvent event = new SearchEvent("video", vod.getId().toString(), action, payload);

        //Send routing-key suitable for action
        String routingKey = prefixSearchRoutingKey + action;
        rabbitTemplate.convertAndSend(vodSearchExchange, routingKey, event);
        log.info("Send event from vod publisher: [{}]", routingKey);
    }


}
