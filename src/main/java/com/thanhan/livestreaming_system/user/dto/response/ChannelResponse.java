package com.thanhan.livestreaming_system.user.dto.response;

import jakarta.annotation.Nullable;

import java.time.Instant;

public record ChannelResponse(
        String id,
        String displayName,
        String description,
        String avatar,
        String bannerUrl,
        Instant createdAt) {
}
