package com.thanhan.livestreaming_system.user.service;

import com.thanhan.livestreaming_system.user.dto.request.ChannelCreationRequest;
import com.thanhan.livestreaming_system.user.dto.request.ChannelUpdateRequest;
import com.thanhan.livestreaming_system.user.dto.response.ChannelAdminResponse;
import com.thanhan.livestreaming_system.user.dto.response.ChannelCacheResponse;
import com.thanhan.livestreaming_system.user.dto.response.ChannelResponse;
import com.thanhan.livestreaming_system.user.entity.Channel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ChannelService {
    ChannelResponse create(String displayName, String description, MultipartFile avatar, MultipartFile banner) throws IllegalAccessException;
    ChannelResponse update(ChannelUpdateRequest request);

    ChannelResponse getChannelById(String id);
    Channel getChannelByOwnerId(String ownerId);
    Long countFollower(Long channelId);
    Channel findById(Long channelId);
    List<ChannelCacheResponse> getFollowingChannels();

    void delete(String channelId);
    List<ChannelAdminResponse> getAllChannels();
    Integer countTotalChannels();
    List<ChannelAdminResponse> searchChannels(String keyword);
}
