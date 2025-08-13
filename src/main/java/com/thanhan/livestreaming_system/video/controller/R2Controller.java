package com.thanhan.livestreaming_system.video.controller;

import com.thanhan.livestreaming_system.common.response.ApiResponse;
import com.thanhan.livestreaming_system.video.dto.MultipartUploadCompleteRequest;
import com.thanhan.livestreaming_system.video.service.R2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.model.CompletedPart;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vods/upload")
@Slf4j
public class R2Controller {

    private final R2Service r2Service;

    @GetMapping("/presigned-url")
    public ApiResponse<String> getPresignedUrl(@RequestParam("fileName") String fileName,
                                               @RequestParam("contentType") String contentType,
                                               @RequestParam("channelId") String channelId) {
        String presignedUrl = r2Service.generatePresignedUrl(fileName, channelId ,contentType);
        return ApiResponse.<String>builder()
                .data(presignedUrl)
                .status(200)
                .build();
    }

    @PostMapping("/multipart/init")
    public ApiResponse<Map<String, String>> initUploadMultipart(@RequestParam("fileName") String fileName,
                                                                @RequestParam("contentType") String contentType,
                                                                @RequestParam("channelId") String channelId) {
        String uploadId = r2Service.initMultipartFileUpload(fileName,channelId ,contentType);
        log.info("[Initmultipart] Upload in: ", fileName);

        Map<String, String> response = new HashMap<>();
        response.put("uploadId", uploadId);
        response.put("keyName", fileName);

        return ApiResponse.<Map<String, String>>builder()
                .status(201)
                .data(response)
                .message("Init upload multipart success!")
                .build();
    }

    @GetMapping("/multipart/presigned-url")
    public ApiResponse<Map<String, String>> getPartUrl(@RequestParam("keyName") String keyName,
                                                       @RequestParam("uploadID") String uploadId,
                                                       @RequestParam("channelId") String channelId,
                                                       @RequestParam("partNo") int partNo) {

        String presignedUrl = r2Service.generatePresignedUrlForEachPart(keyName, channelId, uploadId, partNo);
        Map<String, String> response = new HashMap<>();
        response.put("presignedUrl", presignedUrl);

        return ApiResponse.<Map<String, String>>builder()
                .data(response)
                .status(202)
                .message("Provide presigned url for chunked part no: " + partNo)
                .build();
    }

    @PostMapping("/multipart/complete")
    public ApiResponse<Void> completeUpload(@RequestBody MultipartUploadCompleteRequest request,
                                            @RequestParam("channelId") String channelId,
                                            @RequestParam("vodId") String vodId) {
        List<CompletedPart> completedParts = request.parts().stream()
                .map(p -> CompletedPart.builder()
                        .partNumber(p.partNumber())
                        .eTag(p.eTag())
                        .build())
                .toList();

        log.info("[Complete] Upload completed: ");
        r2Service.completeMultipartUpload(request.keyName(), channelId, vodId, request.uploadId(), completedParts);

        return ApiResponse.<Void>builder()
                .status(204)
                .message("Completed upload multipart video!").build();
    }

}
