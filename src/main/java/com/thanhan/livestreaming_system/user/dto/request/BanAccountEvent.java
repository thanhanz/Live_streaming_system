package com.thanhan.livestreaming_system.user.dto.request;

import lombok.Builder;

@Builder
public record BanAccountEvent(String userId, String email, String subject, String body, String action) {
}
