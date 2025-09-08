package com.thanhan.livestreaming_system.user.dto.mapper;

import com.thanhan.livestreaming_system.user.dto.request.ChannelCreationRequest;
import com.thanhan.livestreaming_system.user.dto.response.ChannelCacheResponse;
import com.thanhan.livestreaming_system.user.dto.response.ChannelCardResponse;
import com.thanhan.livestreaming_system.user.dto.response.ChannelResponse;
import com.thanhan.livestreaming_system.user.dto.response.ChannelToDocumentSearch;
import com.thanhan.livestreaming_system.user.entity.Channel;
import com.thanhan.livestreaming_system.user.entity.User;

import java.time.Instant;

public class ChannelMapper {

    public static ChannelResponse toChannelResponse(Channel channel) {
        return new ChannelResponse(
                channel.getId().toString(),
                channel.getOwner().getId().toString(),
                channel.getDisplayName(),
                channel.getDescription(),
                channel.getAvatarUrl(),
                channel.getBannerUrl(),
                channel.getCreatedAt(),
                channel.getFollowersCount()
        );
    }

    public static ChannelToDocumentSearch toDocumentSearch(Channel channel) {
        return new ChannelToDocumentSearch(
                channel.getId().toString(),
                "channel",
                channel.getDisplayName(),
                channel.getAvatarUrl(),
                Long.valueOf(channel.getFollowersCount())
        );
    }

    public static ChannelCacheResponse toChannelCacheResponse(Channel channel, Long totalFollowers) {
        return new ChannelCacheResponse(
                channel.getId().toString(),
                channel.getDisplayName(),
                channel.getAvatarUrl(),
                channel.getOwner().getId().toString(),
                totalFollowers
        );
    }

    public static ChannelCardResponse toChannelCardResponse(Channel channel) {
        return new ChannelCardResponse(channel.getId().toString(), channel.getDisplayName(), channel.getAvatarUrl());
    }



    public static Channel toChannel(String displayName, String description, User owner) {
        Channel channel = new Channel();
        channel.setOwner(owner);
        channel.setDescription(description);
        channel.setDisplayName(displayName);
        channel.setCreatedAt(Instant.now());
        channel.setUpdatedAt(Instant.now());


        return channel;
    }
}
