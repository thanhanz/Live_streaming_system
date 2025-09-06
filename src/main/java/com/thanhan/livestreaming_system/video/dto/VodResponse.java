package com.thanhan.livestreaming_system.video.dto;

import com.thanhan.livestreaming_system.tag.dto.TagResponse;
import com.thanhan.livestreaming_system.user.dto.response.ChannelCacheResponse;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public record VodResponse(Long id,
                          String title,
                          String description,
                          String thumbnail,
                          Boolean published,
                          Boolean isOnlyMember,
                          Long totalView,
                          Instant createdAt,
                          Set<TagResponse> tags,
                          String url,
                          ChannelCacheResponse channel) {
}
