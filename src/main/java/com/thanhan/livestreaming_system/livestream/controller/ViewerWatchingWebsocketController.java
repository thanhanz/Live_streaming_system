package com.thanhan.livestreaming_system.livestream.controller;


import com.thanhan.livestreaming_system.livestream.utils.StreamCacheKey;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ViewerWatchingWebsocketController {

    SimpMessagingTemplate messagingTemplate;
    RedisTemplate<String, String> redisTemplate;


    @MessageMapping("/viewer/join")
    public void handleJoin(@RequestParam("streamId") String streamId, StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        Long now = System.currentTimeMillis();

        redisTemplate.opsForSet().add("active_stream", streamId);
        redisTemplate.opsForSet().add("live:viewer:" + streamId, sessionId);
        redisTemplate.opsForZSet().add("live:viewer:score:" + streamId, sessionId, now);

        sendToCountCache(streamId);
    }

    //For checking user is watching (in 30s - 60s)
    @MessageMapping("/viewer/heartbeat") //Update ZSet score for each sessionId
    public void handleHeartbeat(@RequestParam("streamId") String streamId, StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        Long now = System.currentTimeMillis();

        redisTemplate.opsForZSet().add("live:viewer:score:" + streamId, sessionId, now);
    }

    private void sendToCountCache(String streamId) {
        long isWatchingCount = redisTemplate.opsForSet().size("live:viewer:" + streamId).intValue();
        messagingTemplate.convertAndSend("/topic/viewers/" + streamId, isWatchingCount);
    }
}
