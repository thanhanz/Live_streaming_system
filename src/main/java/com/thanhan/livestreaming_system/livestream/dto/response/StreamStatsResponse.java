package com.thanhan.livestreaming_system.livestream.dto.response;

public record StreamStatsResponse(
        Integer month,
        Long totalStreams
) {
}
