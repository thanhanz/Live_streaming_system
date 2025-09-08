package com.thanhan.livestreaming_system.user.dto.response;

import java.time.Instant;

public record ChannelCacheResponse(
        String id,
        String displayName,
        String avatar,
        String ownerId,
        Long totalFollowers) {
}
