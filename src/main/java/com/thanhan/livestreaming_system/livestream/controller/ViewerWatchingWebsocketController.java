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

        String key  = StreamCacheKey.isWatchingKey(streamId);
        redisTemplate.opsForHash().put(key, sessionId, String.valueOf(now));

        sendToCountCache(streamId);
    }

    //For checking user is watching (in 30s - 60s)
    @MessageMapping("/viewer/heartbeat") //Check
    public void handleHeartbeat(@RequestParam("streamId") String streamId, StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        Long now = System.currentTimeMillis();

        String key  = StreamCacheKey.isWatchingKey(streamId);
        redisTemplate.opsForHash().put(key, sessionId, String.valueOf(now));
    }

    private void sendToCountCache(String streamId) {
        String key = StreamCacheKey.isWatchingKey(streamId);
        Map<Object, Object> viewers = redisTemplate.opsForHash().entries(key);
        long now = System.currentTimeMillis();
        long validTime = 60000; // 1'

        //Chỉ những session nào đang connect đến stream session trong vòng (validTime) thì mới đếm
        long isWatchingCount = viewers.values()
                .stream()
                .map(Object::toString)
                .mapToLong(Long::parseLong)
                .filter(time -> now - time < validTime)
                .count();

        messagingTemplate.convertAndSend("/topic/viewers/" + streamId, isWatchingCount);
    }

}
