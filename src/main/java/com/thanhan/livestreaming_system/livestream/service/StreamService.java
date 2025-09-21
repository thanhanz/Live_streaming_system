package com.thanhan.livestreaming_system.livestream.service;

import com.thanhan.livestreaming_system.common.paginate.PaginationResponse;
import com.thanhan.livestreaming_system.livestream.dto.request.PaginateGetStreamRequest;
import com.thanhan.livestreaming_system.livestream.dto.request.StreamOnPublishRequest;
import com.thanhan.livestreaming_system.livestream.dto.request.StreamPrepareRequest;
import com.thanhan.livestreaming_system.livestream.dto.response.*;
import com.thanhan.livestreaming_system.livestream.entity.Stream;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface StreamService {
    StreamPrepareResponse prepare(StreamPrepareRequest request, MultipartFile thumbnail);
    boolean isValidStreamKey(String streamKey);
    void finish(String streamKey);
    void startStreaming(String streamKey) throws IOException;
    StreamSessionResponse getStreamById(String streamId);
    Stream getStreamByStreamId(String streamId);
    Boolean isLiveStreaming(String streamKey);
    Stream getLiveStreamByStreamKey(String streamKey);
    List<StreamHistoryResponse> getFinishedStreamByChannelId(Long channelId);

    Set<String> getLiveStreamingChannels();
    StreamCardResponse getCurrentLiveStreaming(Long channelId);

    Long countTotalStreams();
    List<StreamStatsResponse> statisticsStreams(Integer year);

    void deleteLivestream(Long streamId);
    void banStream(String streamId);
    PaginationResponse<StreamAdminResponse> getAllStreams(PaginateGetStreamRequest request);
}
