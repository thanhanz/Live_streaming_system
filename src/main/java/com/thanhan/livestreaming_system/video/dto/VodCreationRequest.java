package com.thanhan.livestreaming_system.video.dto;

public record VodCreationRequest(String title,
                                 String description,
                                 Long channelId) {
}
