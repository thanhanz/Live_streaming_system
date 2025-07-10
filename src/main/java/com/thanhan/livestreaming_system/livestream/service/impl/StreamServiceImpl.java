package com.thanhan.livestreaming_system.livestream.service.impl;

import com.thanhan.livestreaming_system.livestream.dto.mapper.StreamMapper;
import com.thanhan.livestreaming_system.livestream.dto.request.StreamOnPublishRequest;
import com.thanhan.livestreaming_system.livestream.dto.request.StreamPrepareRequest;
import com.thanhan.livestreaming_system.livestream.dto.response.StreamPrepareResponse;
import com.thanhan.livestreaming_system.livestream.dto.response.StreamSessionResponse;
import com.thanhan.livestreaming_system.livestream.entity.Stream;
import com.thanhan.livestreaming_system.livestream.entity.StreamStatus;
import com.thanhan.livestreaming_system.livestream.repository.StreamRepository;
import com.thanhan.livestreaming_system.livestream.service.StreamService;
import com.thanhan.livestreaming_system.user.entity.Channel;
import com.thanhan.livestreaming_system.user.service.ChannelService;
import com.thanhan.livestreaming_system.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StreamServiceImpl implements StreamService {

    StreamRepository streamRepository;
    ChannelService channelService;
    UserService userService;

    @Override
    public StreamPrepareResponse prepare(StreamPrepareRequest request) {
        Channel channel = channelService.findById(request.channelId());

        Stream streamSession = new Stream();
        streamSession.setChannel(channel);
        streamSession.setTitle(channel.getOwner().getLastName() + "'s Live Stream!");
        streamSession.setDescription("Test Live Stream!");
        streamSession.setStatus(StreamStatus.PREPARING);

        String generateStreamKey = UUID.randomUUID().toString();
        String rtmpUrl = "rtmp://localhost:1935/live/";

        streamSession.setStreamKey(generateStreamKey);
        streamSession.setRtmpUrl(rtmpUrl);
        streamRepository.save(streamSession);

        return new StreamPrepareResponse(rtmpUrl, generateStreamKey);
    }

    @Override
    public boolean isValidStreamKey(String streamKey) {
        Stream streamSession = streamRepository.findByStreamKey(streamKey);
        log.info("StreamKey: " + streamSession.getStreamKey());

        if (streamSession == null || streamSession.getStatus() != StreamStatus.PREPARING) {
            log.error("Check stream key: Invalid stream key");
            return false;
        }

        streamSession.setStatus(StreamStatus.STREAMING);
        streamRepository.save(streamSession);
        return true;
    }


    @Override
    public Stream onPublish(StreamOnPublishRequest request) {
//        String username = SecurityContextHolder.getContext().getAuthentication().getName();


        return null;
    }

    @Override
    public void finish(String streamKey) {
        Stream streamSession = streamRepository.findByStreamKey(streamKey);

        if (streamSession == null || streamSession.getStatus() != StreamStatus.STREAMING) {
            log.error("Finish stream: Invalid stream key");
            throw new RuntimeException("Stream key is not valid");
        }

        streamSession.setStatus(StreamStatus.FINISHED);
        streamSession.setEndedAt(Instant.now());
        streamRepository.save(streamSession);
    }

    @Override
    public Stream getStreamById(String streamId) {
        return streamRepository.findById(Long.valueOf(streamId)).orElseThrow(() -> new EntityNotFoundException("Stream not found: " + streamId));
    }

    @Override
    public List<StreamSessionResponse> getAllStreamsByChannelId(String channelId) {

        return streamRepository.findByChannelId(Long.valueOf(channelId))
                .stream().map(StreamMapper::toStreamResponse).collect(Collectors.toList());
    }
}
