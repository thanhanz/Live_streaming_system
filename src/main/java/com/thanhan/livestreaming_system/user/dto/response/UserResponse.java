package com.thanhan.livestreaming_system.user.dto.response;

public record UserResponse (
        String id,
        String username,
        String firstName,
        String lastName,
        String email
) {

}
