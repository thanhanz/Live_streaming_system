package com.thanhan.livestreaming_system.user.dto;

import com.thanhan.livestreaming_system.user.dto.request.UserCreationRequest;
import com.thanhan.livestreaming_system.user.dto.response.UserResponse;
import com.thanhan.livestreaming_system.user.entity.User;

public class UserMapper {

    public static User toUser(UserCreationRequest request) {
        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setUsername(request.username());
        user.setDescription(request.description());
        return user;
    }

    public static UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getDescription()
        );
    }

}
