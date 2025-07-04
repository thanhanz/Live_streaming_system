package com.thanhan.livestreaming_system.user.service;

import com.thanhan.livestreaming_system.user.dto.request.ChannelCreationRequest;
import com.thanhan.livestreaming_system.user.dto.request.ChannelUpdateRequest;
import com.thanhan.livestreaming_system.user.entity.Channel;

public interface ChannelService {
    Channel create(ChannelCreationRequest request);
    Channel update(ChannelUpdateRequest request);
    void delete(Long channelId);
    Channel getChannelById(Long id);

    Channel getChannelByOwnerId(String ownerId);
    Long countFollower(Long channelId);
    String generateStreamKey();
}
