package com.thanhan.livestreaming_system.video.controller;
import com.thanhan.livestreaming_system.common.exception.AppException;
import com.thanhan.livestreaming_system.common.paginate.PaginateParams;
import com.thanhan.livestreaming_system.common.paginate.PaginationResponse;
import com.thanhan.livestreaming_system.common.response.ApiResponse;
import com.thanhan.livestreaming_system.tag.dto.TagRequest;
import com.thanhan.livestreaming_system.video.dto.*;
import com.thanhan.livestreaming_system.video.entity.Vod;
import com.thanhan.livestreaming_system.video.service.R2Service;
import com.thanhan.livestreaming_system.video.service.VodService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CurrentTimestamp;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vods")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VodController {

    VodService vodService;

    @PostMapping("/upload")
    public ApiResponse<String> upload(@RequestParam("thumbnail") MultipartFile thumbnail,
                                      @RequestParam("title") String title,
                                      @RequestParam("channelId") Long channelId,
                                      @RequestParam("description") String description,
                                      @RequestParam(value = "categoryId", required = false) Long categoryId) throws AppException {
        //prepare upload video
        String videoId = vodService.uploadMetadataForVod(new VodCreationRequest(title, description, channelId), thumbnail);

        if (categoryId != null) {
            vodService.assignCategory(Long.valueOf(videoId), categoryId);
        }

        return ApiResponse.<String>builder()
                .status(200)
                .data(videoId)
                .message("Prepare meta data video success!")
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<VodResponse> getVodById(@PathVariable Long id) throws AppException {
        return ApiResponse.<VodResponse>builder()
                .status(200)
                .data(vodService.getVodResById(id))
                .message("Get video id: " + id)
                .build();
    }

    /*
        Paginate
     */
    @GetMapping("/channels/{channelId}")
    ApiResponse<PaginationResponse<VodResponse>> getVodsByChannelId(@PathVariable("channelId") Long channelId ,
                                                                    @RequestParam(required = false) Integer limit,
                                                                    @RequestParam(required = false) String sortBy,
                                                                    @RequestParam(required = false) String order,
                                                                    @RequestParam(required = false) String cursor) {

        VodGetRequest getRequest = VodGetRequest.of(limit, sortBy, order, cursor);

        return ApiResponse.<PaginationResponse<VodResponse>>builder()
                .message("Get paginated vod")
                .data(vodService.getAllVodsByChannelId(channelId, getRequest))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<VodResponse> updateVod(@PathVariable(name = "id") Long id,
                                              @RequestBody VodUpdationRequest request) throws AppException {
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
    public ApiResponse<Void> hideVideo(@PathVariable(name = "id") Long id) throws AppException {
        vodService.hideVod(id);
        return ApiResponse.<Void>builder()
                .message("Hide vod id: " + id)
                .build();
    }

    @PostMapping("/{id}/join")
    public ApiResponse<String> joinVod(@PathVariable(name = "id") Long id,
                                       @RequestBody Map<String, String> params) {
        String key = vodService.initJoinVod(id, params.get("sessionId"));
        return ApiResponse.<String>builder()
                .data(key)
                .status(203).build();
    }

    @PostMapping("/{id}/view")
    public ApiResponse<Void> acceptView(@PathVariable(name = "id") Long id,
                                        @RequestBody Map<String, String> params) {
        String sessionKey = params.get("sessionKey");

        vodService.acceptedViews(id, sessionKey);
        return ApiResponse.<Void>builder()
                .message("Accepted view in session: " + sessionKey)
                .status(203).build();
    }

    @PostMapping("/{id}/tags")
    public ApiResponse<Void> addTags(@PathVariable("id") Long vodId,
                                     @RequestBody TagRequest request) {
        vodService.addTags(vodId, request);
        return ApiResponse.success(204, "Adding tags success!");
    }

    @GetMapping("/hashtag/{tagTitle}")
    public ApiResponse<List<VodResponse>> getVodByTagTitle(@PathVariable("tagTitle") String tagTitle) {

        return ApiResponse.<List<VodResponse>>builder()
                .status(202)
                .data(vodService.getVodsByTagName(tagTitle.toLowerCase())).build();
    }

    @GetMapping("/category/{categoryId}")
    public ApiResponse<List<VodResponse>> getVodsByCategoryId(@PathVariable("categoryId") Long categoryId) {
        return ApiResponse.<List<VodResponse>>builder()
                .status(201)
                .data(vodService.getVodsByCategoryId(categoryId))
                .build();
    }

    /**
     * For administrator
     */

    @GetMapping("/get-all")
    public ApiResponse<PaginationResponse<VodAdminResponse>> getAllVods(@RequestParam(required = false) Integer limit,
                                                                        @RequestParam(required = false) String sortBy,
                                                                        @RequestParam(required = false) String order,
                                                                        @RequestParam(required = false) String cursor) throws AppException {
        VodGetRequest getRequest = VodGetRequest.of(limit, sortBy, order, cursor);

        return ApiResponse.<PaginationResponse<VodAdminResponse>>builder()
                .message("Get paginated vod")
                .data(vodService.getAllVods(getRequest))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<List<VodAdminResponse>> searchVodsWithTitleOrChannelName(@RequestParam String query) throws AppException {
        return ApiResponse.<List<VodAdminResponse>>builder()
                .data(vodService.searchVodsByTitleOrChannelName(query))
                .build();
    }

    @GetMapping("/total")
    public ApiResponse<Long> getTotalNumberOfVods() throws AppException {
        return ApiResponse.<Long>builder()
                .data(vodService.countTotalVods()).build();
    }

    @GetMapping("/stats")
    public ApiResponse<List<VodStatisticResponse>> getVodStats(@RequestParam(value = "year") Integer year) throws AppException {

        return ApiResponse.<List<VodStatisticResponse>>builder()
                .data(vodService.statisticVods(year))
                .build();
    }

}
