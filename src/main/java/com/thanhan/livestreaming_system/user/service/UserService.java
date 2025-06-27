package com.thanhan.livestreaming_system.user.service;

import com.thanhan.livestreaming_system.user.dto.response.UserResponse;
import com.thanhan.livestreaming_system.user.dto.request.UserCreationRequest;


public interface UserService {

    UserResponse register(UserCreationRequest request);

}
