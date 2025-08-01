package com.thanhan.livestreaming_system.video.dto;

public record VodResponse(Long id,
                          String title,
                          String description,
                          String imageUrl,
                          Boolean published,
                          Boolean isOnlyMember,
                          Long channelId) {
}
