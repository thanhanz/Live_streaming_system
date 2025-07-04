package com.thanhan.livestreaming_system.user.dto.request;

import jakarta.annotation.Nullable;

public record ChannelCreationRequest(
        String displayName,
        String description,
        @Nullable String avatar,
        @Nullable String bannerUrl,
        String streamKey //Cai nay se duoc tu dong generate
) {
}
