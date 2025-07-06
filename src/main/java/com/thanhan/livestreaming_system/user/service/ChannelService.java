package com.thanhan.livestreaming_system.user.service;

import com.thanhan.livestreaming_system.user.dto.request.ChannelCreationRequest;
import com.thanhan.livestreaming_system.user.dto.request.ChannelUpdateRequest;
import com.thanhan.livestreaming_system.user.dto.response.ChannelResponse;
import com.thanhan.livestreaming_system.user.entity.Channel;

import java.util.Optional;

public interface ChannelService {
    ChannelResponse create(ChannelCreationRequest request) throws IllegalAccessException;
    ChannelResponse update(ChannelUpdateRequest request);
    void delete(String channelId);
    ChannelResponse getChannelById(String id);
    ChannelResponse getChannelByOwnerId(String ownerId);
    Long countFollower(Long channelId);
    Channel findById(Long channelId);
    String generateStreamKey();
}
