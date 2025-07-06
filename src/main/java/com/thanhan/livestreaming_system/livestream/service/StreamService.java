package com.thanhan.livestreaming_system.livestream.service;

import com.thanhan.livestreaming_system.livestream.dto.request.StreamOnPublishRequest;
import com.thanhan.livestreaming_system.livestream.dto.request.StreamPrepareRequest;
import com.thanhan.livestreaming_system.livestream.dto.response.StreamPrepareResponse;
import com.thanhan.livestreaming_system.livestream.entity.Stream;

public interface StreamService {
    StreamPrepareResponse prepare(StreamPrepareRequest request);
    Stream onPublish(StreamOnPublishRequest streamKey);
    boolean isValidStreamKey(String streamKey);
    void finish(String streamKey);
    Stream getStreamById(String streamId);
}
