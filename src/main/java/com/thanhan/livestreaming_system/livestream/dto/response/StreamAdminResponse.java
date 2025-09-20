package com.thanhan.livestreaming_system.livestream.dto.response;

import java.time.Instant;

public record StreamAdminResponse (
        String id,
        String title,
        String channelName,
        String status,
        Boolean active,
        Long currentViews,
        Instant createdAt,
        Instant endedAt) {
}
