package com.thanhan.livestreaming_system.user.service;

import com.thanhan.livestreaming_system.user.dto.response.UserResponse;
import com.thanhan.livestreaming_system.user.dto.request.UserCreationRequest;
import com.thanhan.livestreaming_system.user.entity.User;


public interface UserService {
    UserResponse register(UserCreationRequest request);
    User getUserByUsername(String username);
    User getUserById(String id);
    String getUserIdByUsername(String username);
}
