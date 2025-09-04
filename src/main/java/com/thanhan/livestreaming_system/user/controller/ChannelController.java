package com.thanhan.livestreaming_system.user.controller;

import com.thanhan.livestreaming_system.common.response.ApiResponse;
import com.thanhan.livestreaming_system.user.dto.request.ChannelCreationRequest;
import com.thanhan.livestreaming_system.user.dto.request.ChannelUpdateRequest;
import com.thanhan.livestreaming_system.user.dto.response.ChannelCacheResponse;
import com.thanhan.livestreaming_system.user.dto.response.ChannelResponse;
import com.thanhan.livestreaming_system.user.entity.Channel;
import com.thanhan.livestreaming_system.user.service.ChannelService;
import com.thanhan.livestreaming_system.user.service.FollowService;
import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChannelController {

    ChannelService channelService;


    @PostMapping
    public ApiResponse<ChannelResponse> createChannel(@RequestParam("avatar") MultipartFile avatar,
                                                      @RequestParam(value = "banner", required = false) MultipartFile banner ,
                                                      @RequestParam("displayName") String displayName,
                                                      @RequestParam("description") String description) throws IllegalAccessException {
        return ApiResponse.<ChannelResponse>builder()
                .status(200)
                .message("Channel created successfully!")
                .data(channelService.create(displayName, description, avatar, banner))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ChannelResponse> getChannelById(@PathVariable("id") String channelId) {
        return ApiResponse.<ChannelResponse>builder()
                .status(200)
                .data(channelService.getChannelById(channelId))
                .message("Get channel successfully!")
                .build();
    }

    @GetMapping("/following")
    public ApiResponse<List<ChannelCacheResponse>> getFollowingChannels() {
        return ApiResponse.<List<ChannelCacheResponse>>builder()
                .message("Get list following channels")
                .status(200)
                .data(channelService.getFollowingChannels())
                .build();
    }

    @PutMapping
    public ApiResponse<ChannelResponse> updateMyChannel(@RequestBody ChannelUpdateRequest request) throws IllegalAccessException {
        return ApiResponse.<ChannelResponse>builder()
                .message("Updated channel successfully!")
                .status(201)
                .data(channelService.update(request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteChannel(@PathVariable("id") String channelId) {
        channelService.delete(channelId);
        return ApiResponse.success(204, "Deleted channel successfully!");
    }

    @GetMapping("/follower/{id}")
    public ApiResponse<Long> getNumberOfFollowers(@PathVariable("id") String channelId) {
        return ApiResponse.<Long>builder()
                .status(201)
                .data(channelService.countFollower(Long.valueOf(channelId)))
                .build();
    }
}
