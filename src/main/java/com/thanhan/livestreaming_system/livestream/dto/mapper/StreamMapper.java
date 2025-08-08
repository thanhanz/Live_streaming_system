package com.thanhan.livestreaming_system.livestream.dto.mapper;

import com.thanhan.livestreaming_system.livestream.dto.response.StreamSessionResponse;
import com.thanhan.livestreaming_system.livestream.entity.Stream;
import com.thanhan.livestreaming_system.user.dto.mapper.ChannelMapper;

public class StreamMapper {

    public static StreamSessionResponse toStreamResponse(Stream stream, Integer currentViewer, Long totalFollowers) {
        return new StreamSessionResponse(
                stream.getId().toString(),
                stream.getChannel().getOwner().getId().toString(),
                stream.getTitle(),
                stream.getDescription(),
                stream.getThumbnailUrl(),
                stream.getStatus().name(),
                stream.getCreatedAt(),
                stream.getEndedAt(),
                stream.getStreamKey(),
                currentViewer,
                ChannelMapper.toChannelCacheResponse(stream.getChannel(), totalFollowers)
        );
    }

}
