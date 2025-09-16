package com.thanhan.livestreaming_system.user.dto.response;

import com.thanhan.livestreaming_system.user.entity.Role;

import java.util.List;
import java.util.Set;

public record UserResponse (
        String id,
        String username,
        Boolean active,
        String avatar,
        String firstName,
        String lastName,
        String email,
        Set<String> roles
) {

}
