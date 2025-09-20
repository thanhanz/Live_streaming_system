package com.thanhan.livestreaming_system.livestream.dto.response;

import com.thanhan.livestreaming_system.user.dto.response.ChannelCacheResponse;

import java.time.Instant;

public record StreamSessionResponse(
        String id,
        String ownerId,
        String title,
        String description,
        String thumbnailUrl,
        String status,
        Boolean active,
        Instant createdAt,
        Instant endedAt,
        String streamKey,
        Integer currentViewer,
        ChannelCacheResponse channel) {
}
