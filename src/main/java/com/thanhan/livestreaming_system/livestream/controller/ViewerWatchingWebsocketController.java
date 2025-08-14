package com.thanhan.livestreaming_system.livestream.controller;


import com.thanhan.livestreaming_system.livestream.utils.StreamCacheKey;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ViewerWatchingWebsocketController {

    SimpMessagingTemplate messagingTemplate;
    RedisTemplate<String, String> redisTemplate;


    @MessageMapping("/viewer/join")
    public void handleJoin(@Payload Map<String, String> payload, StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        Long now = System.currentTimeMillis();
        String streamId = payload.get("streamId");
        String concurrencyViewersKey = StreamCacheKey.cacheConcurrencyViewers(streamId);
        log.info("StreamId: " + streamId + ", and sessionId: " + sessionId);

        redisTemplate.opsForSet().add("active_stream", streamId);
        redisTemplate.opsForZSet().add(concurrencyViewersKey, sessionId, now);

        Boolean exists = redisTemplate.hasKey(concurrencyViewersKey);
        log.info("Redis key live:viewer:" + streamId + " exists =" + exists);

        sendConcurrencyViewersToSub(streamId);
    }

    //For checking user is watching (in 30s - 60s)
    @MessageMapping("/viewer/heartbeat") //Update ZSet score for each sessionId
    public void handleHeartbeat(@Payload Map<String, String> payload, StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        Long now = System.currentTimeMillis();
        String streamId = payload.get("streamId");
        String concurrencyViewersKey = StreamCacheKey.cacheConcurrencyViewers(streamId);
        redisTemplate.opsForZSet().add(concurrencyViewersKey, sessionId, now);
    }

    private void sendConcurrencyViewersToSub(String streamId) {
        String concurrencyViewersKey = StreamCacheKey.cacheConcurrencyViewers(streamId);
        long isWatchingCount = redisTemplate.opsForZSet().size(concurrencyViewersKey).intValue();
        messagingTemplate.convertAndSend("/livestream/topic/viewers/" + streamId, isWatchingCount);
    }
}
