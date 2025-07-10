package com.thanhan.livestreaming_system.livestream.dto.mapper;

import com.thanhan.livestreaming_system.livestream.dto.response.StreamSessionResponse;
import com.thanhan.livestreaming_system.livestream.entity.Stream;

public class StreamMapper {

    public static StreamSessionResponse toStreamResponse(Stream stream) {
        return new StreamSessionResponse(
                stream.getId().toString(),
                stream.getTitle(),
                stream.getDescription(),
                stream.getThumbnailUrl(),
                stream.getStatus().name(),
                stream.getCreatedAt(),
                stream.getEndedAt()
        );
    }

}
