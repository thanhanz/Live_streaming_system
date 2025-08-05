package com.thanhan.livestreaming_system.membership.controller;

import com.thanhan.livestreaming_system.common.response.ApiResponse;
import com.thanhan.livestreaming_system.membership.dto.request.MembPackageCreateRequest;
import com.thanhan.livestreaming_system.membership.dto.response.MembPackageResponse;
import com.thanhan.livestreaming_system.membership.service.MembershipPackageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/channels/membership-packages")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MembershipPackageController {

    MembershipPackageService membershipPackageService;

    @GetMapping("/{id}")
    public ApiResponse<MembPackageResponse> getPackageById(@PathVariable("id") String packageId) {

        MembPackageResponse result = membershipPackageService.getPackage(Long.valueOf(packageId));

        return ApiResponse.<MembPackageResponse>builder()
                .message("Get package success with id: " + result.id())
                .data(result)
                .status(201)
                .build();
    }

    @PostMapping("/")
    public ApiResponse<MembPackageResponse> createPackage(@RequestBody MembPackageCreateRequest request) {
        MembPackageResponse result = membershipPackageService.createMembershipPackage(request);
        return ApiResponse.<MembPackageResponse>builder()
                .message("Create new package: " + result.id())
                .data(result)
                .status(201)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePackage(@PathVariable("id") String packageId) {
        membershipPackageService.deletePackage(Long.valueOf(packageId));
        return ApiResponse.success(200, "Deleted package: " + packageId);
    }
}
