package com.thanhan.livestreaming_system.livestream.service.impl;

import com.thanhan.livestreaming_system.livestream.service.LiveWebSocketService;
import com.thanhan.livestreaming_system.livestream.utils.StreamCacheKey;
import com.thanhan.livestreaming_system.user.utils.ChannelUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LiveWebSocketServiceImpl implements LiveWebSocketService {

    SimpMessagingTemplate messagingTemplate;
    RedisTemplate<String, String> redisTemplate;

    @Override
    public void sentLiveStreamStatus(String channelId, String status) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("status", status);
        payload.put("channelId", channelId);
        messagingTemplate.convertAndSend("/livestream/topic/streaming-channels", payload);

        String liveStreamingChannelsKey = StreamCacheKey.isLivestreamingChannels();
        if (status.equals("streaming"))
            redisTemplate.opsForSet().add(liveStreamingChannelsKey, channelId);
        else
            redisTemplate.opsForSet().remove(liveStreamingChannelsKey, channelId);
    }
}
