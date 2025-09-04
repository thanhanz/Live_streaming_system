package com.thanhan.livestreaming_system.user.dto.request;

import jakarta.annotation.Nullable;

public record ChannelCreationRequest(
        String displayName,
        String description
        ) {
}
