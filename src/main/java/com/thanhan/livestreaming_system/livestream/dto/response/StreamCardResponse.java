package com.thanhan.livestreaming_system.livestream.dto.response;

import com.thanhan.livestreaming_system.user.dto.response.ChannelCacheResponse;
import com.thanhan.livestreaming_system.user.dto.response.ChannelCardResponse;

import java.time.Instant;

public record StreamCardResponse(
        String id,
        String title,
        String thumbnail,
        String status,
        Integer currentViewer,
        ChannelCardResponse channel
) {
}
