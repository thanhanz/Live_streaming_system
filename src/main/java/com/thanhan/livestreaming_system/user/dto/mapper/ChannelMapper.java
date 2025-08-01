package com.thanhan.livestreaming_system.user.dto.mapper;

import com.thanhan.livestreaming_system.user.dto.request.ChannelCreationRequest;
import com.thanhan.livestreaming_system.user.dto.response.ChannelResponse;
import com.thanhan.livestreaming_system.user.entity.Channel;
import com.thanhan.livestreaming_system.user.entity.User;

import java.time.Instant;

public class ChannelMapper {

    public static ChannelResponse toChannelResponse(Channel channel) {
        return new ChannelResponse(
                channel.getId().toString(),
                channel.getDisplayName(),
                channel.getDescription(),
                channel.getAvatarUrl(),
                channel.getBannerUrl(),
                channel.getCreatedAt()
        );
    }

    public static Channel toChannel(ChannelCreationRequest request, User owner) {
        Channel channel = new Channel();
        channel.setOwner(owner);
        channel.setAvatarUrl(request.avatar());
        channel.setBannerUrl(request.bannerUrl());
        channel.setDescription(request.description());
        channel.setDisplayName(request.displayName());
        channel.setCreatedAt(Instant.now());
        channel.setUpdatedAt(Instant.now());


        return channel;
    }
}
