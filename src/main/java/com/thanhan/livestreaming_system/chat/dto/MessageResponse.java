package com.thanhan.livestreaming_system.chat.dto;

import com.thanhan.livestreaming_system.user.dto.response.UserResponse;
import com.thanhan.livestreaming_system.user.entity.User;

import java.time.Instant;

public record MessageResponse (
        String id,
        String content,
        UserResponse sender,
        String streamId,
        Instant createdAt
) {
}
