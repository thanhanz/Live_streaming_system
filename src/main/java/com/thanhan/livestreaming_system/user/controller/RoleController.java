package com.thanhan.livestreaming_system.user.controller;

import com.thanhan.livestreaming_system.common.response.ApiResponse;
import com.thanhan.livestreaming_system.user.dto.request.RoleRequest;
import com.thanhan.livestreaming_system.user.entity.Role;
import com.thanhan.livestreaming_system.user.service.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/roles")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleController {
    RoleService roleService;

    @PostMapping
    ApiResponse<Role> create(@RequestBody RoleRequest request) {
        return ApiResponse.<Role>builder()
                .data(roleService.createRole(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<Role>> getAll() {
        return ApiResponse.<List<Role>>builder()
                .data(roleService.getAll())
                .build();
    }

    @DeleteMapping("/{roleId}")
    ApiResponse<Void> delete(@PathVariable String roleId) {
        roleService.deleteRole(roleId);
        return ApiResponse.<Void>builder().build();
    }

}
