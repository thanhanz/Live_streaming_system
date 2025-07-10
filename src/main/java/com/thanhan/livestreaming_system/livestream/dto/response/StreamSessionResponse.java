package com.thanhan.livestreaming_system.livestream.dto.response;

import java.time.Instant;

public record StreamSessionResponse(
        String id,
        String title,
        String description,
        String thumbnailUrl,
        String status,
        Instant createdAt,
        Instant endedAt) {
}
