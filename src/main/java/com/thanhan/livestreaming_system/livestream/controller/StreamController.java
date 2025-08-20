package com.thanhan.livestreaming_system.livestream.controller;

import com.thanhan.livestreaming_system.common.response.ApiResponse;
import com.thanhan.livestreaming_system.livestream.dto.request.StreamPrepareRequest;
import com.thanhan.livestreaming_system.livestream.dto.response.StreamHistoryResponse;
import com.thanhan.livestreaming_system.livestream.dto.response.StreamPrepareResponse;
import com.thanhan.livestreaming_system.livestream.dto.response.StreamSessionResponse;
import com.thanhan.livestreaming_system.livestream.entity.Stream;
import com.thanhan.livestreaming_system.livestream.messaging.StreamTranscodeConsumer;
import com.thanhan.livestreaming_system.livestream.messaging.StreamTranscodeProducer;
import com.thanhan.livestreaming_system.livestream.service.FFmpegService;
import com.thanhan.livestreaming_system.livestream.service.LiveWebSocketService;
import com.thanhan.livestreaming_system.livestream.service.StreamService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/stream")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

@CrossOrigin(origins = "http://localhost:3000", exposedHeaders = "Content-Disposition")
public class StreamController {

    private static final Logger log = LoggerFactory.getLogger(StreamController.class);
    final StreamService streamService;
    final S3Client s3Client;
    final StreamTranscodeProducer streamTranscodeProducer;
    private final LiveWebSocketService websocketService;

    @Value("${cloudflare.r2.bucket}")
    String R2Bucket;

    @GetMapping("/{id}")
    @CrossOrigin(originPatterns = "http://localhost:3000")
    public ApiResponse<StreamSessionResponse> getStreamById(@PathVariable("id") String streamId) {
        return ApiResponse.<StreamSessionResponse>builder()
                .status(201)
                .data(streamService.getStreamById(streamId)).build();
    }

    @PostMapping("/prepare")
    public ApiResponse<StreamPrepareResponse> prepare(@RequestParam("thumbnail") MultipartFile thumbnail,
                                                      @RequestParam("title") String title,
                                                      @RequestParam("channelId") Long channelId,
                                                      @RequestParam("description") String description) {

        return ApiResponse.<StreamPrepareResponse>builder()
                .data(streamService.prepare(new StreamPrepareRequest(channelId, title, description), thumbnail))
                .status(200)
                .message("Preparing you streaming")
                .build();
    }

    @GetMapping("/livestreaming_channels")
    public ApiResponse<Set<String>> getLiveStreamingChannels() {
        return ApiResponse.<Set<String>>builder()
                .status(200)
                .data(streamService.getLiveStreamingChannels())
                .build();
    }

    @PostMapping("/on_publish")
    public ApiResponse<Void> onPublish(@RequestParam("name") String request) throws IOException {
        log.info("Start: authen stream key");
        if (!streamService.isValidStreamKey(request)) {
            log.error("Reject: Invalid stream key");
            return ApiResponse.<Void>builder()
                    .status(401)
                    .message("Invalid stream key")
                    .build();
        }

        log.info("Accept: Valid stream key");
        streamTranscodeProducer.sendMessage(request);

        Stream stream = streamService.getLiveStreamByStreamKey(request);
        String channelId = stream.getChannel().getId().toString();
        websocketService.sentLiveStreamStatus(channelId, "streaming");

        return ApiResponse.<Void>builder()
                .status(200)
                .message("You are publishing")
                .build();
    }

    @PostMapping("/finish")
    public ApiResponse<Void> finish(@RequestParam("name") String streamKey) {
        streamService.finish(streamKey);
        Stream stream = streamService.getLiveStreamByStreamKey(streamKey);
        String channelId = stream.getChannel().getId().toString();
        websocketService.sentLiveStreamStatus(channelId, "stopped");
        log.info("Send message: [STOPPED] with channelId: " + channelId);
        return ApiResponse.<Void>builder()
                .status(200)
                .message("Your stream has been finished")
                .build();
    }

    @GetMapping("/history")
    public ApiResponse<List<StreamHistoryResponse>> getStreamHistory(@RequestParam("channelId") Long channelId) {
        return ApiResponse.<List<StreamHistoryResponse>>builder()
                .data(streamService.getFinishedStreamByChannelId(channelId))
                .status(200)
                .message("Get list history!")
                .build();
    }


    @PostMapping("/download")
    public ResponseEntity<Resource> downloadRecordingLivestream(@RequestParam("key") String key) {
        log.info("Start download record video from stream key: " + key);
        try {
            String r2StorageRecording = "recordings/" + key + "/";

            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(R2Bucket)
                    .prefix(r2StorageRecording)
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(request);

            List<S3Object> allMp4Object = response.contents().stream()
                    .filter(obj -> obj.key().endsWith(".mp4"))
                    .collect(Collectors.toList());

            if (allMp4Object.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            File zipFile = File.createTempFile("recordings-" + key + "-", ".zip");

            try (FileOutputStream fos = new FileOutputStream(zipFile);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {

                // Stream trực tiếp từ S3 vào ZIP
                for (S3Object obj : allMp4Object) {
                    String fileName = Paths.get(obj.key()).getFileName().toString();

                    GetObjectRequest getRequest = GetObjectRequest.builder()
                            .bucket(R2Bucket)
                            .key(obj.key())
                            .build();

                    try (ResponseInputStream<GetObjectResponse> s3In = s3Client.getObject(getRequest)) {
                        ZipEntry zipEntry = new ZipEntry(fileName);
                        zos.putNextEntry(zipEntry);

                        // Copy trực tiếp từ S3 stream vào ZIP
                        byte[] buffer = new byte[8192]; // Buffer lớn hơn cho hiệu suất tốt hơn
                        int length;
                        while ((length = s3In.read(buffer)) > 0) {
                            zos.write(buffer, 0, length);
                        }

                        zos.closeEntry();
                        log.info("Added file to ZIP: " + fileName);
                    }
                }
            }

            Resource resource = new FileSystemResource(zipFile);
            String zipFileName = "recordings-" + key + ".zip";
            zipFile.deleteOnExit();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + zipFileName + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/zip")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(zipFile.length()))
                    .body(resource);

        } catch (Exception e) {
            log.error("Error downloading and zipping recordings", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}