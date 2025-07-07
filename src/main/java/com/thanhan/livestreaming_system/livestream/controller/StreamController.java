package com.thanhan.livestreaming_system.livestream.controller;

import com.thanhan.livestreaming_system.common.response.ApiResponse;
import com.thanhan.livestreaming_system.livestream.dto.request.StreamOnPublishRequest;
import com.thanhan.livestreaming_system.livestream.dto.request.StreamPrepareRequest;
import com.thanhan.livestreaming_system.livestream.dto.response.StreamPrepareResponse;
import com.thanhan.livestreaming_system.livestream.entity.Stream;
import com.thanhan.livestreaming_system.livestream.service.FFmpegService;
import com.thanhan.livestreaming_system.livestream.service.StreamService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stream")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StreamController {

    private static final Logger log = LoggerFactory.getLogger(StreamController.class);
    StreamService streamService;
    FFmpegService ffmpegService;

    @GetMapping("/{id}")
    public ApiResponse<Stream> getStreamById(@PathVariable("id") String streamId) {
        return ApiResponse.<Stream>builder()
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
        ffmpegService.startTranscode(request);

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

}