package com.thanhan.livestreaming_system.user.dto.request;

import jakarta.annotation.Nullable;

public record ChannelUpdateRequest(
        String displayName,
        String description,
        @Nullable String avatar,
        @Nullable String bannerUrl
) {
}
