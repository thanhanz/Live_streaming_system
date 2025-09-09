package com.thanhan.livestreaming_system.user.service.impl;

import com.thanhan.livestreaming_system.common.exception.AppException;
import com.thanhan.livestreaming_system.common.exception.ErrorCode;
import com.thanhan.livestreaming_system.user.dto.mapper.UserMapper;
import com.thanhan.livestreaming_system.user.dto.response.UserResponse;
import com.thanhan.livestreaming_system.user.entity.Role;
import com.thanhan.livestreaming_system.user.entity.User;
import com.thanhan.livestreaming_system.user.dto.request.UserCreationRequest;
import com.thanhan.livestreaming_system.user.repository.RoleRepository;
import com.thanhan.livestreaming_system.user.repository.UserRepository;
import com.thanhan.livestreaming_system.user.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;

    @Override
    public UserResponse register(UserCreationRequest request) {

        if(userRepository.existsByUsername(request.username()))
            throw new AppException(ErrorCode.USER_EXISTED);
        if (userRepository.existsByEmail(request.email()))
            throw new RuntimeException("Email has used by others users");

        User user = UserMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role role = roleRepository.findByName("USER").orElseThrow(() -> new RuntimeException("Role not found"));
        user.getRoles().add(role);

        var savedUser = userRepository.save(user);

        return UserMapper.toUserResponse(savedUser);
    }

    @Override
    public UserResponse updateRoleUser(User user, String roleName) {
        log.info("Get role: ", roleName);
        Role role = roleRepository.findByName(roleName).orElseThrow(() -> new RuntimeException("Role not exist"));

        user.getRoles().add(role);

        return UserMapper.toUserResponse(userRepository.save(user));
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

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUser() {
        return userRepository.findAll().stream().map(UserMapper::toUserResponse).collect(Collectors.toList());
    }
}
