package com.thanhan.livestreaming_system.user.dto.response;

public record ChannelToDocumentSearch(
        String entityId,
        String type,
        String channelName,
        String channelAvatar,
        Long channelFollowers
) {
}
