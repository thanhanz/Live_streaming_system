package com.thanhan.livestreaming_system.video.dto;

import com.thanhan.livestreaming_system.user.dto.response.ChannelCacheResponse;

import java.time.Instant;

public record VodResponse(Long id,
                          String title,
                          String description,
                          String thumbnail,
                          Boolean published,
                          Boolean isOnlyMember,
                          Long totalView,
                          Instant createdAt,
                          ChannelCacheResponse channel) {
}
