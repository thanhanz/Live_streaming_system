package com.thanhan.livestreaming_system.video.dto;

import com.thanhan.livestreaming_system.user.dto.response.ChannelCacheResponse;

public record VodResponse(Long id,
                          String title,
                          String description,
                          String imageUrl,
                          Boolean published,
                          Boolean isOnlyMember,
                          Long channelId,
                          ChannelCacheResponse channel) {
}
