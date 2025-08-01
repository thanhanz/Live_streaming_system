package com.thanhan.livestreaming_system.video.dto;

public record VodUpdationRequest(String title,
                                 String description,
                                 String imageUrl,
                                 Boolean isOnlyMember,
                                 Boolean published) {
}
