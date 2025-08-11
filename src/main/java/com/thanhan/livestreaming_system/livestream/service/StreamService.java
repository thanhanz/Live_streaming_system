package com.thanhan.livestreaming_system.livestream.service;

import com.thanhan.livestreaming_system.livestream.dto.request.StreamOnPublishRequest;
import com.thanhan.livestreaming_system.livestream.dto.request.StreamPrepareRequest;
import com.thanhan.livestreaming_system.livestream.dto.response.StreamPrepareResponse;
import com.thanhan.livestreaming_system.livestream.dto.response.StreamSessionResponse;
import com.thanhan.livestreaming_system.livestream.entity.Stream;

import java.util.List;
import java.util.Set;

public interface StreamService {
    StreamPrepareResponse prepare(StreamPrepareRequest request);
    boolean isValidStreamKey(String streamKey);
    void finish(String streamKey);
    StreamSessionResponse getStreamById(String streamId);
    Stream getStreamByStreamId(String streamId);
    Boolean isLiveStreaming(String streamKey);
    Stream getLiveStreamByStreamKey(String streamKey);
    void uploadRecordLivestreamToR2(String streamKey);

    Set<String> getLiveStreamingChannels();
}
