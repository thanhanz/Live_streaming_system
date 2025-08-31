package com.thanhan.livestreaming_system.membership.controller;
import com.thanhan.livestreaming_system.common.response.ApiResponse;
import com.thanhan.livestreaming_system.membership.service.UserMembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/membership")
@RequiredArgsConstructor
public class UserMembershipController {

    private final UserMembershipService userMembershipService;

    @GetMapping("/channel/{channelId}/check")
    public ApiResponse<Boolean> checkMembership(@PathVariable("channelId") String channelId,
                                                @RequestParam("userId") String userId) {
        return ApiResponse.<Boolean>builder()
                .status(201)
                .data(userMembershipService.checkMembership(Long.valueOf(channelId), UUID.fromString(userId)))
                .build();
    }
}
