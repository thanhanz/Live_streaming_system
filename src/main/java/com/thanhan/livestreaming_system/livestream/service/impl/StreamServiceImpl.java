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
import com.thanhan.livestreaming_system.livestream.utils.StreamCacheKey;
import com.thanhan.livestreaming_system.user.entity.Channel;
import com.thanhan.livestreaming_system.user.service.ChannelService;
import com.thanhan.livestreaming_system.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StreamServiceImpl implements StreamService {

    final StreamRepository streamRepository;
    final ChannelService channelService;
    final RedisTemplate<String, String> redisTemplate;
    final S3Client s3Client;

    @Value("${cloudflare.r2.bucket}")
    String R2Bucket;

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
    public void finish(String streamKey) {
        Stream streamSession = streamRepository.findByStreamKey(streamKey);

        if (streamSession == null || streamSession.getStatus() != StreamStatus.STREAMING) {
            log.error("Finish stream: Invalid stream key");
            throw new RuntimeException("Stream key is not valid");
        }

        streamSession.setStatus(StreamStatus.FINISHED);
        streamSession.setEndedAt(Instant.now());

        //Upload record livestream to R2
        uploadRecordLivestreamToR2(streamKey);

        redisTemplate.opsForSet().remove("active_streams", streamSession.getId());
        redisTemplate.opsForSet().remove("live:viewer:" + streamSession.getId());
        redisTemplate.opsForZSet().remove("live:viewer:score" + streamSession.getId());
        redisTemplate.opsForSet().remove("chat:banned:" + streamSession.getId());

        log.info("Removed from cache: " + streamSession.getId());

        streamRepository.save(streamSession);
    }

    @Override
    public void uploadRecordLivestreamToR2(String streamKey) {
        String storageRecordPath = "/var/www/html/hls/" + streamKey;
        File folder = new File(storageRecordPath);

        if (!folder.exists() || !folder.isDirectory()) {
            throw new RuntimeException("Folder not found in: " + storageRecordPath);
        }

        File[] files = folder.listFiles((dir, name) -> name.matches("recording_\\d{3}\\.mp4"));

        if (files == null || files.length == 0) {
            throw new RuntimeException("No .mp4 recordings found for streamKey: " + streamKey);
        }

        Arrays.sort(files);
        log.info("Found " + files.length + " recordings in: " + storageRecordPath);
        for (File file : files) {
            String key = "recordings/" + streamKey + "/" + file.getName();
            try {
                PutObjectRequest putRequest = PutObjectRequest.builder()
                        .bucket(R2Bucket)
                        .key(key)
                        .contentType("video/mp4")
                        .build();

                s3Client.putObject(putRequest, RequestBody.fromFile(file.toPath()));
                log.info("Uploaded: {}", file.getName());

            } catch (Exception e) {
                log.error("Failed to upload file: {}", file.getName(), e);
            }
        }
    }

    @Override
    public StreamSessionResponse getStreamById(String streamId) {
        Stream stream = streamRepository.findById(Long.valueOf(streamId)).orElseThrow(() -> new EntityNotFoundException("Stream not found: " + streamId));
        Integer currentViewer = redisTemplate.opsForSet().size("live:viewer:" + stream.getId().toString()).intValue();
        return StreamMapper.toStreamResponse(stream, currentViewer);
    }

    @Override
    public Stream getStreamByStreamId(String streamId) {
        return streamRepository.findById(Long.valueOf(streamId)).orElseThrow(() -> new EntityNotFoundException("Stream not found: " + streamId));
    }

    @Override
    public List<StreamSessionResponse> getAllStreamsByChannelId(String channelId) {

        return streamRepository.findByChannelId(Long.valueOf(channelId))
                .stream().map((Stream stream) -> {
                    Integer currentViewer = redisTemplate.opsForSet().size("live:viewer:" + stream.getId().toString()).intValue();
                    return StreamMapper.toStreamResponse(stream, currentViewer);
                } ).collect(Collectors.toList());
    }

    @Override
    public Boolean isLiveStreaming(String streamKey) {
        Stream streamSession = streamRepository.findByStreamKey(streamKey);
        return streamSession != null && streamSession.getStatus() == StreamStatus.STREAMING;
    }
}
