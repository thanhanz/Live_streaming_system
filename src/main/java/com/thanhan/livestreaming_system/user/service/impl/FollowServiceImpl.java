package com.thanhan.livestreaming_system.user.service.impl;

import com.thanhan.livestreaming_system.common.exception.AppException;
import com.thanhan.livestreaming_system.common.exception.ErrorCode;
import com.thanhan.livestreaming_system.user.entity.Channel;
import com.thanhan.livestreaming_system.user.entity.Follow;
import com.thanhan.livestreaming_system.user.entity.User;
import com.thanhan.livestreaming_system.user.repository.ChannelRepository;
import com.thanhan.livestreaming_system.user.repository.FollowRepository;
import com.thanhan.livestreaming_system.user.repository.UserRepository;
import com.thanhan.livestreaming_system.user.service.FollowService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FollowServiceImpl implements FollowService {

    FollowRepository followRepository;
    UserRepository userRepository;
    ChannelRepository channelRepository;

    @Override
    public void follow(String userId, String channelId) {
        User user = userRepository.findById(UUID.fromString(userId)).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST));
        Channel channel = channelRepository.findById(Long.valueOf(channelId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        boolean checkFollowed = followRepository.existsByFollowerAndChannel(user, channel);

        if (checkFollowed) {
            throw new IllegalArgumentException("User already followed this channel");
        }

        Follow follow = new Follow();
        follow.setFollower(user);
        follow.setChannel(channel);
        follow.setFollowedAt(Instant.now());
        this.followRepository.save(follow);
    }

    @Override
    public void unfollow(String userId, String channelId) {
        User user = userRepository.findById(UUID.fromString(userId)).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST));
        Channel channel = channelRepository.findById(Long.valueOf(channelId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        boolean checkFollowed = followRepository.existsByFollowerAndChannel(user, channel);
        if (!checkFollowed)
            throw new IllegalArgumentException("User isn't follow this channel");
        this.followRepository.deleteByFollowerAndChannel(user, channel);
    }

    @Override
    public boolean isFollowing(String userId, String channelId) {

        User user = userRepository.findById(UUID.fromString(userId)).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST));
        Channel channel = channelRepository.findById(Long.valueOf(channelId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        return this.followRepository.existsByFollowerAndChannel(user, channel);
    }
}
