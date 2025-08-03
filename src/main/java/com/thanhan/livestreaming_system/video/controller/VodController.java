package com.thanhan.livestreaming_system.video.controller;
import com.thanhan.livestreaming_system.common.exception.AppException;
import com.thanhan.livestreaming_system.common.paginate.PaginationResponse;
import com.thanhan.livestreaming_system.common.response.ApiResponse;
import com.thanhan.livestreaming_system.video.dto.*;
import com.thanhan.livestreaming_system.video.service.VodService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @GetMapping("/{id}")
    public ApiResponse<VodResponse> getVodById(@PathVariable Long id) throws AppException {
        return ApiResponse.<VodResponse>builder()
                .status(200)
                .data(vodService.getVodById(id))
                .message("Get video id: " + id)
                .build();
    }

    /*
        Paginate
     */
    @GetMapping("/channels/{channelId}")
    public ApiResponse<PaginationResponse<VodResponse>> getVodsByChannelId(@PathVariable(name = "channelId") Long channelId, @RequestBody(required = false) VodGetRequest request) throws AppException  {
        return ApiResponse.<PaginationResponse<VodResponse>>builder()
                .message("Get paginated vod")
                .data(vodService.getAllVodsByChannelId(channelId, request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<VodResponse> updateVod(@PathVariable(name = "id") Long id, VodUpdationRequest request) throws AppException {
        VodResponse result = vodService.updateVod(id, request);

        return ApiResponse.<VodResponse>builder()
                .message("Updated vod " + id + " success!")
                .data(result)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteVod(@PathVariable(name = "id") Long id) throws AppException {
        vodService.deleteVod(id);
        return ApiResponse.success(204, "Deleted vod " + id);
    }

    @PutMapping("/{id}/hide")
    public ApiResponse<VodResponse> hideVideo(@PathVariable(name = "id") Long id) throws AppException {
        return ApiResponse.<VodResponse>builder()
                .message("Hide vod id: " + id)
                .data(vodService.hideVod(id))
                .build();
    }


}
