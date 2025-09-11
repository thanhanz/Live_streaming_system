package com.thanhan.livestreaming_system.livestream.service.impl;

import com.thanhan.livestreaming_system.chat.utils.ChatUtils;
import com.thanhan.livestreaming_system.livestream.dto.mapper.StreamMapper;
import com.thanhan.livestreaming_system.livestream.dto.request.StreamOnPublishRequest;
import com.thanhan.livestreaming_system.livestream.dto.request.StreamPrepareRequest;
import com.thanhan.livestreaming_system.livestream.dto.response.*;
import com.thanhan.livestreaming_system.livestream.entity.Stream;
import com.thanhan.livestreaming_system.livestream.entity.StreamStatus;
import com.thanhan.livestreaming_system.livestream.repository.StreamRepository;
import com.thanhan.livestreaming_system.livestream.service.StreamService;
import com.thanhan.livestreaming_system.livestream.utils.StreamCacheKey;
import com.thanhan.livestreaming_system.user.entity.Channel;
import com.thanhan.livestreaming_system.user.service.ChannelService;
import com.thanhan.livestreaming_system.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StreamServiceImpl implements StreamService {

    final StreamRepository streamRepository;
    final ChannelService channelService;
    final RedisTemplate<String, String> redisTemplate;
    final RedisTemplate<String, Object> objectRedisTemplate;
    final S3Client s3Client;

    @Value("${cloudflare.r2.bucket}")
    String R2Bucket;

    @Value("${cloudflare.r2.public-url-id}")
    private String publicR2Id;

    /*
        Thay = ten domain chu khong nen su dung Id nay`
     */
    public String getPublicR2Url() {
        return "https://" + publicR2Id + ".r2.dev/";
    }

    @Override
    public StreamPrepareResponse prepare(StreamPrepareRequest request, MultipartFile thumbnail) {
        Channel channel = channelService.findById(request.channelId());

        Stream streamSession = new Stream();
        streamSession.setChannel(channel);
        streamSession.setTitle(request.title());
        streamSession.setDescription(request.description());
        streamSession.setStatus(StreamStatus.PREPARING);
        String generateStreamKey = UUID.randomUUID().toString();
        String rtmpUrl = "rtmp://localhost:1935/live/";

        streamSession.setStreamKey(generateStreamKey);
        streamSession.setRtmpUrl(rtmpUrl);

        String storageThumbnail = uploadThumbnailToR2(request.channelId(), thumbnail);

        /*
            Set prefix to cache thumbnail from R2
         */
        String urlThumbnail = getPublicR2Url() + storageThumbnail;
        streamSession.setThumbnailUrl(urlThumbnail);
        streamRepository.save(streamSession);

        return new StreamPrepareResponse(rtmpUrl, generateStreamKey);
    }

    private String uploadThumbnailToR2(Long channelId, MultipartFile thumbnail) {
        String rawKey = "vods/thumbnail/" + channelId + "/" + System.currentTimeMillis() + "_" + thumbnail.getName();
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(R2Bucket)
                    .key(rawKey)
                    .contentType(thumbnail.getContentType())
                    .build();

            byte[] bytes = thumbnail.getBytes();
            s3Client.putObject(putRequest, RequestBody.fromBytes(bytes));

            log.info("Uploaded: {}", thumbnail.getName());
        } catch (Exception e) {
            log.error("Failed to upload file: {}", thumbnail.getName(), e);
        }
        return rawKey;
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
    @Transactional
    public void finish(String streamKey) {
        Stream streamSession = streamRepository.findByStreamKey(streamKey);

        if (streamSession == null || streamSession.getStatus() != StreamStatus.STREAMING) {
            log.error("Finish stream: Invalid stream key");
            throw new RuntimeException("Stream key is not valid");
        }

        streamSession.setStatus(StreamStatus.FINISHED);
        streamSession.setEndedAt(Instant.now());
        streamRepository.save(streamSession);

//        uploadRecordLivestreamToR2(streamKey);

        String concurrencyViewersKey = StreamCacheKey.cacheConcurrencyViewers(streamSession.getId().toString());
        String listBannedKey = ChatUtils.bannedChatKey(streamSession.getId().toString());

        if (redisTemplate.hasKey(concurrencyViewersKey)) {
            redisTemplate.opsForZSet().remove(concurrencyViewersKey);
        }

        if (redisTemplate.hasKey(listBannedKey))
            redisTemplate.opsForSet().remove(listBannedKey);

        log.info("Success upload to R2 with streamKey: " + streamSession.getStreamKey());
    }

    private void uploadRecordLivestreamToR2(String streamKey) {
        String storageRecordPath = "/var/www/html/hls/" + streamKey;
        File folder = new File(storageRecordPath);

        if (!folder.exists() || !folder.isDirectory()) {
            throw new RuntimeException("Folder not found in: " + storageRecordPath);
        }

        File[] files = folder.listFiles((dir, name) -> name.matches("recording.mp4"));

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
    @Transactional
    public StreamSessionResponse getStreamById(String streamId) {
        Stream stream = streamRepository.findById(Long.valueOf(streamId)).orElseThrow(() -> new EntityNotFoundException("Stream not found: " + streamId));

        if (stream.getStatus() != StreamStatus.STREAMING) {
            throw new RuntimeException("Stream is not publish yet: " + streamId);
        }

        Integer currentViewer = redisTemplate.opsForSet().size("live:viewer:" + stream.getId().toString()).intValue();
        Long totalFollowers = channelService.countFollower(stream.getChannel().getId());
        return StreamMapper.toStreamResponse(stream, currentViewer, totalFollowers);
    }

    @Override
    public Stream getStreamByStreamId(String streamId) {
        return streamRepository.findById(Long.valueOf(streamId)).orElseThrow(() -> new EntityNotFoundException("Stream not found: " + streamId));
    }

    @Override
    public Stream getLiveStreamByStreamKey(String streamKey) {
        return streamRepository.findByStreamKey(streamKey);
    }


    @Override
    public Boolean isLiveStreaming(String streamKey) {
        Stream streamSession = streamRepository.findByStreamKey(streamKey);
        return streamSession != null && streamSession.getStatus() == StreamStatus.STREAMING;
    }

    @Override
    public Set<String> getLiveStreamingChannels() {
        String liveStreamingChannelsKey = StreamCacheKey.isLivestreamingChannels();
        if (!redisTemplate.hasKey(liveStreamingChannelsKey))
            return new HashSet<>();
        Set<String> results = redisTemplate.opsForSet().members(liveStreamingChannelsKey);

        if (results == null || results.size() == 0) {
            return new HashSet<>();
        }
        return results;
    }

    @Override
    public List<StreamHistoryResponse> getFinishedStreamByChannelId(Long channelId) {
        return streamRepository.findFinishedStreamByChannelId(channelId)
                .stream()
                .map(StreamMapper::toStreamHistoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public StreamCardResponse getCurrentLiveStreaming(Long channelId) {
        String currentLivestreamCache = StreamCacheKey.isLivestreamingChannels();
        boolean isStreaming = Boolean.TRUE.equals(objectRedisTemplate.opsForSet().isMember(currentLivestreamCache, channelId.toString()));

        log.info("Channel [{}] streaming: {}", channelId, isStreaming);

        if (!isStreaming) {
            throw new RuntimeException("Current livestreaming channel is not streaming: " + channelId);
        }

        Stream stream = streamRepository.getCurrentLiveStreaming(channelId);
        if (stream == null) {
            throw new RuntimeException("Live stream not found in channel: " + channelId);
        }

        Integer currentViewer = redisTemplate.opsForSet().size("live:viewer:" + stream.getId().toString()).intValue();

        return StreamMapper.toStreamCardResponse(stream, currentViewer);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Long countTotalStreams() {
        return streamRepository.countTotalStreams();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<StreamStatsResponse> statisticsStreams(Integer year) {
        List<StreamStatsResponse> rawData;
        if (year == null) {
            rawData = streamRepository.statisticStreams(LocalDateTime.now().getYear());
        } else rawData = streamRepository.statisticStreams(year);

        List<StreamStatsResponse> result = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            Integer month = i + 1;
            Long totalStreams = rawData.stream()
                    .filter(s -> s.month().equals(month))
                    .map(StreamStatsResponse::totalStreams)
                    .findFirst().orElse(0L);
            result.add(new StreamStatsResponse(month, totalStreams));
        }
        return result;
    }
}
