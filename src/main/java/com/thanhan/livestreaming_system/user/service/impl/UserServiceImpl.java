package com.thanhan.livestreaming_system.user.service.impl;

import com.thanhan.livestreaming_system.common.exception.AppException;
import com.thanhan.livestreaming_system.common.exception.ErrorCode;
import com.thanhan.livestreaming_system.user.dto.mapper.UserMapper;
import com.thanhan.livestreaming_system.user.dto.response.UserResponse;
import com.thanhan.livestreaming_system.user.entity.User;
import com.thanhan.livestreaming_system.user.dto.request.UserCreationRequest;
import com.thanhan.livestreaming_system.user.repository.UserRepository;
import com.thanhan.livestreaming_system.user.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;

    PasswordEncoder passwordEncoder;

    @Override
    public UserResponse register(UserCreationRequest request) {

        if(userRepository.existsByUsername(request.username()))
            throw new RuntimeException("KAKAKAK");

        User user = UserMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {

            log.error(e.getMessage());
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        return UserMapper.toUserResponse(user);
    }

    @Override
    public User getUserByUsername(String username) {
          return userRepository.findByUsername(username).orElseThrow(() ->
                    new AppException(ErrorCode.USER_NOT_EXIST));
    }

    @Override
    public User getUserById(String id) {
        return userRepository.findById(UUID.fromString(id)).orElseThrow(() ->
                new AppException(ErrorCode.USER_NOT_EXIST));
    }

    @Override
    public String getUserIdByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new AppException(ErrorCode.USER_NOT_EXIST));
        return user.getId().toString();
    }
}
