package com.thanhan.livestreaming_system.user.dto.mapper;

import com.thanhan.livestreaming_system.user.dto.request.UserCreationRequest;
import com.thanhan.livestreaming_system.user.dto.response.UserResponse;
import com.thanhan.livestreaming_system.user.entity.Role;
import com.thanhan.livestreaming_system.user.entity.User;

import java.util.stream.Collectors;

public class UserMapper {

    public static User toUser(UserCreationRequest request) {
        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setUsername(request.username());
        return user;
    }

    public static UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId().toString(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
        );
    }

}
