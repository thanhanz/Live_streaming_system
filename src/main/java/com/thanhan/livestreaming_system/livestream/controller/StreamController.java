package com.thanhan.livestreaming_system.livestream.controller;

import com.thanhan.livestreaming_system.common.response.ApiResponse;
import com.thanhan.livestreaming_system.livestream.dto.request.StreamOnPublishRequest;
import com.thanhan.livestreaming_system.livestream.dto.request.StreamPrepareRequest;
import com.thanhan.livestreaming_system.livestream.dto.response.StreamPrepareResponse;
import com.thanhan.livestreaming_system.livestream.dto.response.StreamSessionResponse;
import com.thanhan.livestreaming_system.livestream.entity.Stream;
import com.thanhan.livestreaming_system.livestream.service.FFmpegService;
import com.thanhan.livestreaming_system.livestream.service.StreamService;
import com.thanhan.livestreaming_system.livestream.utils.StreamCacheKey;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/stream")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StreamController {

    private static final Logger log = LoggerFactory.getLogger(StreamController.class);
    StreamService streamService;
    FFmpegService ffmpegService;

    @GetMapping("/{id}")
    @CrossOrigin(originPatterns = "http://localhost:3000")
    public ApiResponse<StreamSessionResponse> getStreamById(@PathVariable("id") String streamId) {
        return ApiResponse.<StreamSessionResponse>builder()
                .status(201)
                .data(streamService.getStreamById(streamId)).build();
    }

    @GetMapping("/prepare")
    public ApiResponse<StreamPrepareResponse> prepare(@RequestBody StreamPrepareRequest request) {
        return ApiResponse.<StreamPrepareResponse>builder()
                .data(streamService.prepare(request))
                .status(200)
                .message("Preparing you streaming")
                .build();
    }

    @PostMapping("/on_publish")
    public ApiResponse<Void> onPublish(@RequestParam("name") String request) {
        log.info("Start: authen stream key");
        if (!streamService.isValidStreamKey(request)) {
            log.error("Reject: Invalid stream key");
            return ApiResponse.<Void>builder()
                    .status(401)
                    .message("Invalid stream key")
                    .build();
        }

        log.info("Accept: Valid stream key");
        ffmpegService.transcodeToHls(request);
//        ffmpegService.transcodeToDash(request);

        return ApiResponse.<Void>builder()
                .status(200)
                .message("You are publishing")
                .build();
    }

    @PostMapping("/finish")
    public ApiResponse<Void> finish(@RequestParam("name") String streamKey) {
        streamService.finish(streamKey);

        return ApiResponse.<Void>builder()
                .status(200)
                .message("Your stream has been finished")
                .build();
    }

    @PostMapping("/record") //Should be path variable
    public ApiResponse<Void> startRecording(@RequestParam("key") String key) {
        log.info("Start recording video from stream key: " + key);
        if (!streamService.isLiveStreaming(key)) {
            throw new RuntimeException("Reject: You are not live streaming");
        }

        ffmpegService.startRecording(key);
        return ApiResponse.<Void>builder()
                .status(200)
                .message("You are recording a livestream")
                .build();
    }

    @PostMapping("/download") // -Path variable
    public ResponseEntity<Resource> downloadRecordingLivestream(@RequestParam("key") String key) {
        log.info("Start dowload record video from stream key: " + key);
        try {
            String outputDir = "/var/www/html/hls/" + key;

            File dir = new File(outputDir);
            File[] mp4Files = dir.listFiles((d, name) -> name.endsWith(".mp4"));

            if (mp4Files == null || mp4Files.length == 0) {
                return ResponseEntity.notFound().build();
            }

            File latestFile = Arrays.stream(mp4Files)
                    .max(Comparator.comparingLong(File::lastModified))
                    .orElse(null);

            Resource resource = new FileSystemResource(latestFile);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + latestFile.getName() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "video/mp4")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error downloading recording", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}