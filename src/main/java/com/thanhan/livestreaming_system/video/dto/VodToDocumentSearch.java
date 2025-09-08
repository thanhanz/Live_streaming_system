package com.thanhan.livestreaming_system.video.dto;
import java.time.Instant;

public record VodToDocumentSearch(
        String entityId,
        String type,
        String title,
        String description,
        String thumbnailUrl,
        Boolean isOnlyMember,
        Long viewCount,
        Instant createdAt,
        String channelId,
        String channelName,
        String channelAvatar,
        Long channelFollowers
) {
}
