package com.thanhan.livestreaming_system.livestream.controller;

import com.thanhan.livestreaming_system.common.paginate.PaginationResponse;
import com.thanhan.livestreaming_system.common.response.ApiResponse;
import com.thanhan.livestreaming_system.livestream.dto.request.PaginateGetStreamRequest;
import com.thanhan.livestreaming_system.livestream.dto.request.StreamPrepareRequest;
import com.thanhan.livestreaming_system.livestream.dto.response.*;
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
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class StreamController {

    private static final Logger log = LoggerFactory.getLogger(StreamController.class);
    final StreamService streamService;
    final S3Client s3Client;

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

    @GetMapping("/channel/{channelId}/streaming")
    public ApiResponse<StreamCardResponse> getCurrentLiveStreaming(@PathVariable("channelId") Long channelId) {
        return ApiResponse.<StreamCardResponse>builder()
                .data(streamService.getCurrentLiveStreaming(channelId))
                .message("Current live streaming")
                .status(200)
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

        streamService.startStreaming(request);

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

    @GetMapping("/history")
    public ApiResponse<List<StreamHistoryResponse>> getStreamHistory(@RequestParam("channelId") Long channelId) {
        return ApiResponse.<List<StreamHistoryResponse>>builder()
                .data(streamService.getFinishedStreamByChannelId(channelId))
                .status(200)
                .message("Get list history!")
                .build();
    }

    @GetMapping("/home")
    public ApiResponse<List<StreamCardResponse>> getAllLiveStreaming() {
        return ApiResponse.<List<StreamCardResponse>>builder()
                .data(streamService.getAllLivestreamingCards())
                .build();
    }
    @CrossOrigin(origins = "http://localhost:3000", exposedHeaders = "Content-Disposition")
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

            S3Object mp4Object = allMp4Object.get(0);
            String fileName = Paths.get(mp4Object.key()).getFileName().toString();

            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(R2Bucket)
                    .key(mp4Object.key())
                    .build();

            ResponseInputStream<GetObjectResponse> s3InputStream = s3Client.getObject(getRequest);

            File tempFile = File.createTempFile("records-", ".mp4");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = s3InputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
            s3InputStream.close();

            Resource resource = new FileSystemResource(tempFile);
            tempFile.deleteOnExit();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "video/mp4")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(tempFile.length()))
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error downloading recordings", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/count")
    public ApiResponse<Long> countStreams() {
        return ApiResponse.<Long>builder()
                .status(200)
                .data(streamService.countTotalStreams())
                .build();
    }

    @GetMapping("/stats")
    public ApiResponse<List<StreamStatsResponse>> getStreamStats(@RequestParam("year") Integer year) {
        return ApiResponse.<List<StreamStatsResponse>>builder()
                .status(201)
                .data(streamService.statisticsStreams(year)).build();
    }
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteLiveStream(@PathVariable("id") Long streamId) {
        streamService.deleteLivestream(streamId);
        return ApiResponse.success(204, "Deleted livestream: " + streamId);
    }

    @GetMapping("/get-all")
    public ApiResponse<PaginationResponse<StreamAdminResponse>> getAllStreams(@RequestParam(required = false) Integer limit,
                                                         @RequestParam(required = false) String sortBy,
                                                         @RequestParam(required = false) String order,
                                                         @RequestParam(required = false) String cursor) {
        PaginateGetStreamRequest request = PaginateGetStreamRequest.of(limit, sortBy, order, cursor);

        return ApiResponse.<PaginationResponse<StreamAdminResponse>>builder()
                .data(streamService.getAllStreams(request))
                .status(201).build();
    }

    @PostMapping("/ban")
    public ApiResponse<Void> banStream(@RequestParam("streamId") String streamId) {
        streamService.banStream(streamId);

        log.info("Banned stream: " + streamId);
        return ApiResponse.success(204, "Banned!");
    }
}
