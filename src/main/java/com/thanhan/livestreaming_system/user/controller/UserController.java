package com.thanhan.livestreaming_system.user.controller;

import com.thanhan.livestreaming_system.common.response.ApiResponse;
import com.thanhan.livestreaming_system.user.dto.mapper.ChannelMapper;
import com.thanhan.livestreaming_system.user.dto.mapper.UserMapper;
import com.thanhan.livestreaming_system.user.dto.response.ChannelResponse;
import com.thanhan.livestreaming_system.user.dto.response.UserResponse;
import com.thanhan.livestreaming_system.user.entity.Channel;
import com.thanhan.livestreaming_system.user.entity.User;
import com.thanhan.livestreaming_system.user.dto.request.UserCreationRequest;
import com.thanhan.livestreaming_system.user.service.ChannelService;
import com.thanhan.livestreaming_system.user.service.FollowService;
import com.thanhan.livestreaming_system.user.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;
    FollowService followService;
    ChannelService channelService;

    @GetMapping("/current-user")
    public ApiResponse<UserResponse> getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        return ApiResponse.<UserResponse>builder()
                .data(UserMapper.toUserResponse(userService.getUserByUsername(username)))
                .status(202)
                .build();
    }

    @GetMapping("/my-channel")
    public ApiResponse<ChannelResponse> getMyChannel() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByUsername(username);
        Channel c = channelService.getChannelByOwnerId(user.getId().toString());
        return ApiResponse.<ChannelResponse>builder()
                .status(201)
                .message("Get my channel")
                .data(ChannelMapper.toChannelResponse(c))
                .build();
    }

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@RequestBody @Valid UserCreationRequest request) {

        return ApiResponse.<UserResponse>builder()
                .message("SUCCESS")
                .data(userService.register(request))
                .status(200)
                .build();
    }

    @PostMapping("/channel/{id}/follow")
    public ApiResponse<Void> follow(@PathVariable(name = "id") String channelId) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User u = userService.getUserByUsername(username);

        followService.follow(u.getId().toString(), channelId);
        return ApiResponse.<Void>builder()
                .message("Follow successfully!").status(200).build();
    }

    @PostMapping("/channel/{id}/unfollow")
    public ApiResponse<Void> unfollow(@PathVariable(name = "id") String channelId) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User u = userService.getUserByUsername(username);

        followService.unfollow(u.getId().toString(), channelId);

        return ApiResponse.<Void>builder()
                .message("Unfollow channel successfully!").status(200).build();
    }

    @GetMapping("/channel/{id}")
    public ApiResponse<Boolean> isFollowing(@PathVariable(name = "id") String channelId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User u = userService.getUserByUsername(username);

        return ApiResponse.<Boolean>builder()
                .status(200)
                .message("Check follow")
                .data(this.followService.isFollowing(u.getId().toString(), channelId))
                .build();
    }
}
