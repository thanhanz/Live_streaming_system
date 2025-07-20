package com.thanhan.livestreaming_system.livestream.controller;


import com.thanhan.livestreaming_system.livestream.utils.StreamCacheKey;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;

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

        log.info("StreamId: " + streamId + ", and sessionId: " + sessionId);

        redisTemplate.opsForSet().add("active_stream", streamId);
        redisTemplate.opsForSet().add("live:viewer:" + streamId, sessionId);
        redisTemplate.opsForZSet().add("live:viewer:score:" + streamId, sessionId, now);

        Boolean exists = redisTemplate.hasKey("live:viewer:" + streamId);
        log.info("📦 Redis key live:viewer:" + streamId + " exists =m" + exists);

        sendToCountCache(streamId);
    }

    //For checking user is watching (in 30s - 60s)
    @MessageMapping("/viewer/heartbeat") //Update ZSet score for each sessionId
    public void handleHeartbeat(@Payload Map<String, String> payload, StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        Long now = System.currentTimeMillis();
        String streamId = payload.get("streamId");

        redisTemplate.opsForZSet().add("live:viewer:score:" + streamId, sessionId, now);
    }

    private void sendToCountCache(String streamId) {
        long isWatchingCount = redisTemplate.opsForSet().size("live:viewer:" + streamId).intValue();
        messagingTemplate.convertAndSend("/livestream/topic/viewers/" + streamId, isWatchingCount);
    }
}
