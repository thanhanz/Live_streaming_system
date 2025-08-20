package com.thanhan.livestreaming_system.video.dto;

import com.thanhan.livestreaming_system.user.dto.response.ChannelCacheResponse;
import com.thanhan.livestreaming_system.video.entity.Vod;
import com.thanhan.livestreaming_system.video.utils.VodsRedisKey;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

@RequiredArgsConstructor
public class VodMapper {

    public static VodResponse toVodResponse(Vod vod, Long view, ChannelCacheResponse channel) {
        return new VodResponse(
                vod.getId(),
                vod.getTitle(),
                vod.getDescription(),
                vod.getThumbnail(),
                vod.getPublished(),
                vod.getOnlyMember(),
                view,
                vod.getCreatedAt(),
                vod.getVideoUrl(),
                channel
        );
    }

}
