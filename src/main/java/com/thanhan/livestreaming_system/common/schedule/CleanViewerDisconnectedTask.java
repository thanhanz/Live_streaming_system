package com.thanhan.livestreaming_system.common.schedule;

import com.thanhan.livestreaming_system.livestream.utils.StreamCacheKey;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CleanViewerDisconnectedTask {

    RedisTemplate<String, String> redisTemplate;
    SimpMessagingTemplate messagingTemplate;
    long validTimeSchedule = 45000;

    @Scheduled(fixedRate = 45000) //45s
    public void cleanDisconnect() { //In redis
        long now = System.currentTimeMillis();
        long validTime = validTimeSchedule; //Neu' session nao khong hoat dong trong 30s se bi xoa'
        Set<String> isLiveStream = redisTemplate.opsForSet().members("active_stream");

        if (isLiveStream != null && isLiveStream.size() > 0) { //Co nguoi dang live stream
            for (String streamId : isLiveStream) {
                String zSetSessionScore = StreamCacheKey.cacheConcurrencyViewers(streamId);
                Set<String> expiredSessionIds = redisTemplate.opsForZSet()
                        .rangeByScore(zSetSessionScore, 0, now - validTime);
                if (expiredSessionIds != null && expiredSessionIds.size() > 0) {
                    redisTemplate.opsForZSet().remove(zSetSessionScore, expiredSessionIds.toArray());
                }

                long concurrencyViewers = redisTemplate.opsForZSet().size(zSetSessionScore).intValue();
                messagingTemplate.convertAndSend("/livestream/topic/viewers/" + streamId, concurrencyViewers);
            }
        }
    }
}
