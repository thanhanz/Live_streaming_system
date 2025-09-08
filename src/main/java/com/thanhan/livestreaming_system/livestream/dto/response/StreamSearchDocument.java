package com.thanhan.livestreaming_system.livestream.dto.response;

import com.thanhan.livestreaming_system.user.dto.response.ChannelCardResponse;

public record StreamSearchDocument(
        String title,
        String thumbnailUrl,
        String channelId,
        String channelName,
        String avatarUrl
) {
}
