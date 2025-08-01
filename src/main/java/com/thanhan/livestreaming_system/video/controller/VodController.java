package com.thanhan.livestreaming_system.video.controller;
import com.thanhan.livestreaming_system.common.exception.AppException;
import com.thanhan.livestreaming_system.common.response.ApiResponse;
import com.thanhan.livestreaming_system.video.dto.VodCreationRequest;
import com.thanhan.livestreaming_system.video.dto.VodResponse;
import com.thanhan.livestreaming_system.video.entity.Vod;
import com.thanhan.livestreaming_system.video.service.VodService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/vods")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VodController {

    VodService vodService;

    @PostMapping("/upload")
    public ApiResponse<VodResponse> upload(@RequestParam("video") MultipartFile file, VodCreationRequest request) throws AppException {
        return ApiResponse.<VodResponse>builder()
                .status(200)
                .data(vodService.uploadVod(request, file))
                .message("Upload video success!")
                .build();
    }


}
