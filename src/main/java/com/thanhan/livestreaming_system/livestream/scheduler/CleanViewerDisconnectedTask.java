package com.thanhan.livestreaming_system.livestream.scheduler;

import com.thanhan.livestreaming_system.livestream.utils.StreamCacheKey;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CleanViewerDisconnectedTask {

    RedisTemplate<String, String> redisTemplate;
    SimpMessagingTemplate messagingTemplate;
    long validTimeSchedule = 45000;

    //Loop 45s/time
    @Scheduled(fixedRate = 45000)
    public void cleanDisconnect() { //In redis
        long now = System.currentTimeMillis();
        long validTime = validTimeSchedule;
        Set<String> isLiveStream = redisTemplate.opsForSet().members("active_stream");

        if (isLiveStream != null && isLiveStream.size() > 0) { //Co nguoi dang live stream
            for (String streamId : isLiveStream) {
                String zSetSessionScore = StreamCacheKey.cacheConcurrencyViewers(streamId);
                Set<String> expiredSessionIds = redisTemplate.opsForZSet()
                        .rangeByScore(zSetSessionScore, 0, now - validTime); //Lấy tất cả sessionId đã không xem trong 45s (validTime)
                if (expiredSessionIds != null && expiredSessionIds.size() > 0) {
                    redisTemplate.opsForZSet().remove(zSetSessionScore, expiredSessionIds.toArray());
                }

                //Cache concurrencyViewrs
                long concurrencyViewers = redisTemplate.opsForZSet().size(zSetSessionScore).intValue();
                messagingTemplate.convertAndSend("/livestream/topic/viewers/" + streamId, concurrencyViewers);
            }
        }
    }
}
