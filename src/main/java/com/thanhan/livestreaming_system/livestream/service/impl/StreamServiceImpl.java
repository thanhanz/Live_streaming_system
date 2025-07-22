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
import org.springframework.data.redis.core.RedisTemplate;
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
    RedisTemplate<String, String> redisTemplate;
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

        redisTemplate.opsForSet().remove("active_streams", streamSession.getId());
        redisTemplate.opsForSet().remove("live:viewer:" + streamSession.getId());
        redisTemplate.opsForZSet().remove("live:viewer:score" + streamSession.getId());
        log.info("Removed from cache: " + streamSession.getId());
        streamRepository.save(streamSession);
    }

//    private void downloadRecordLivestream(String streamKey) {
//        String outputDir = "/var/www/html/hls/" + streamKey;
//
//        // Find latest MP4 file
//        File dir = new File(outputDir);
//        File[] mp4Files = dir.listFiles((d, name) -> name.endsWith(".mp4"));
//
//        if (mp4Files == null || mp4Files.length == 0) {
//            return ResponseEntity.notFound().build();
//        }
//
//        // Get latest file
//        File latestFile = Arrays.stream(mp4Files)
//                .max(Comparator.comparingLong(File::lastModified))
//                .orElse(null);
//
//        if (latestFile == null) {
//            return ResponseEntity.notFound().build();
//        }
//
//        Resource resource = new FileSystemResource(latestFile);
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION,
//                        "attachment; filename=\"" + latestFile.getName() + "\"")
//                .header(HttpHeaders.CONTENT_TYPE, "video/mp4")
//                .body(resource);
//
//    } catch (Exception e) {
//        log.error("Error downloading recording", e);
//        return ResponseEntity.internalServerError().build();
//    }
//}
//    }

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
