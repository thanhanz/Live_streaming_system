package com.thanhan.livestreaming_system.livestream.dto.mapper;

import com.thanhan.livestreaming_system.livestream.dto.response.*;
import com.thanhan.livestreaming_system.livestream.entity.Stream;
import com.thanhan.livestreaming_system.livestream.entity.StreamStatus;
import com.thanhan.livestreaming_system.livestream.utils.StreamCacheKey;
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
                stream.getActive(),
                stream.getCreatedAt(),
                stream.getEndedAt(),
                stream.getStreamKey(),
                currentViewer,
                ChannelMapper.toChannelCacheResponse(stream.getChannel(), totalFollowers)
        );
    }

    public static StreamSearchDocument toStreamSearchDocument(Stream stream) {
        return new StreamSearchDocument(
                stream.getTitle(),
                stream.getThumbnailUrl(),
                stream.getChannel().getId().toString(),
                stream.getChannel().getDisplayName(),
                stream.getChannel().getAvatarUrl()
        );
    }

    public static StreamCardResponse toStreamCardResponse(Stream stream, Integer currentViewer) {
        return new StreamCardResponse(
                stream.getId().toString(),
                stream.getTitle(),
                stream.getThumbnailUrl(),
                stream.getStatus().name(),
                currentViewer,
                ChannelMapper.toChannelCardResponse(stream.getChannel())
        );
    }

    public static StreamHistoryResponse toStreamHistoryResponse(Stream stream) {
        return new StreamHistoryResponse(
                stream.getId().toString(),
                stream.getChannel().getOwner().getId().toString(),
                stream.getTitle(),
                stream.getDescription(),
                stream.getThumbnailUrl(),
                stream.getStatus().name(),
                stream.getCreatedAt(),
                stream.getEndedAt(),
                stream.getStreamKey()
        );
    }

    public static StreamAdminResponse toAdminResponse(Stream stream, Long currentViews) {

        return new StreamAdminResponse(
                stream.getId().toString(),
                stream.getTitle(),
                stream.getChannel().getDisplayName(),
                stream.getStatus().name(),
                stream.getActive(),
                currentViews,
                stream.getCreatedAt(),
                stream.getEndedAt()
        );
    }

}
