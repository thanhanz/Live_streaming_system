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

    @Scheduled(fixedRate = 30000) //30s
    public void cleanDisconnect() { //In redis
        long now = System.currentTimeMillis();
        long validTime = 60000; //Neu' session nao khong hoat dong trong 60s se bi xoa'
        Set<String> isLiveStream = redisTemplate.opsForSet().members("active_stream");

        if (isLiveStream != null && isLiveStream.size() > 0) { //Co nguoi dang live stream
            for (String streamId : isLiveStream) {
                String setSessionId = "live:viewer:" + streamId;
                String zSetSessionScore = "live:viewer:score:" + streamId;


                Set<String> expiredSessionIds = redisTemplate.opsForZSet()
                        .rangeByScore(zSetSessionScore, 0, now - validTime);

                if (expiredSessionIds != null && expiredSessionIds.size() > 0) {
                    redisTemplate.opsForSet().remove(setSessionId, expiredSessionIds.toArray());
                    redisTemplate.opsForZSet().remove(zSetSessionScore, expiredSessionIds.toArray());
                }

                Long viewerCount = redisTemplate.opsForSet().size("live:viewer:" + streamId);
                messagingTemplate.convertAndSend("/topic/viewers/" + streamId, viewerCount);
            }
        }
    }

}
