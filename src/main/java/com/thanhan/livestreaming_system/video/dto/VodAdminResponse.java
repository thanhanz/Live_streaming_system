package com.thanhan.livestreaming_system.video.dto;

import com.thanhan.livestreaming_system.tag.dto.TagResponse;
import com.thanhan.livestreaming_system.user.dto.response.ChannelCacheResponse;

import java.time.Instant;
import java.util.Set;

public record VodAdminResponse (
        Long id,
        String title,
        Long totalView,
        Instant createdAt,
        String channelName
) {
}
