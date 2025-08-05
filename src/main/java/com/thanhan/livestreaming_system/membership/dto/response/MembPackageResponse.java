package com.thanhan.livestreaming_system.membership.dto.response;

import com.thanhan.livestreaming_system.user.dto.response.ChannelResponse;

public record MembPackageResponse(Long id, String name, String description, int price, int duration) {
}
