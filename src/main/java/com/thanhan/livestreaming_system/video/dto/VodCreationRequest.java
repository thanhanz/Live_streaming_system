package com.thanhan.livestreaming_system.video.dto;

public record VodCreationRequest(String title,
                                 String description,
                                 String imageUrl,
                                 Long channelId) {
}
