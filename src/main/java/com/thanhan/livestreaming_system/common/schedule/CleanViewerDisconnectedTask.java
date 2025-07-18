package com.thanhan.livestreaming_system.common.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CleanViewerDisconnectedTask {

    RedisTemplate<String, String> redisTemplate;
    SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedRate = 10000) //10s
    public void cleanDisconnect() {
        long now = System.currentTimeMillis();
        Set<String> keys = redisTemplate.keys("viewer:*");

        for (String key : keys) {
            Map<Object, Object> streamEntries = redisTemplate.opsForHash().entries(key);
            String getStreamId = key.replace("viewer:", "");
            boolean changed = false;

            for (Map.Entry<Object, Object> entry : streamEntries.entrySet()) {
                long timestamp = Long.parseLong(String.valueOf(entry.getValue()));
                if (now - timestamp > 60000) { //60s
                    redisTemplate.opsForHash().delete(key, entry.getKey());
                    changed = true;
                }
            }

            if (changed) {
                long count = redisTemplate.opsForHash().size(key);
                messagingTemplate.convertAndSend("/topic/streams/" + getStreamId, Map.of("count", String.valueOf(count)));
            }
        }

    }

}
