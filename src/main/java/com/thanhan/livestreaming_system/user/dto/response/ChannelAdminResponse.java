package com.thanhan.livestreaming_system.user.dto.response;

public record ChannelAdminResponse(
        Long channelId,
        String displayName,
        String ownerName,
        Integer totalFollowers,
        Long totalVods,
        Long totalLivestream
) {
}
