package com.thanhan.livestreaming_system.video.dto;

import com.thanhan.livestreaming_system.tag.dto.TagResponse;
import com.thanhan.livestreaming_system.user.dto.response.ChannelCacheResponse;
import com.thanhan.livestreaming_system.video.entity.Vod;
import com.thanhan.livestreaming_system.video.utils.VodsRedisKey;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class VodMapper {

    public static VodResponse toVodResponse(Vod vod, Long view, ChannelCacheResponse channel) {
        Set<TagResponse> tags = new HashSet<>();
        if (vod.getTags().size() > 0) {
            vod.getTags().forEach(tag -> {
                tags.add(new TagResponse(tag.getId(), tag.getTitle()));
            });
        }
        return new VodResponse(
                vod.getId(),
                vod.getTitle(),
                vod.getDescription(),
                vod.getThumbnail(),
                vod.getPublished(),
                vod.getOnlyMember(),
                view,
                vod.getCreatedAt(),
                tags,
                vod.getVideoUrl(),
                channel
        );
    }

}
