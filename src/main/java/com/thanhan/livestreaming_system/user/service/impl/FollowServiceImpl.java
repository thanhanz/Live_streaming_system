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
import com.thanhan.livestreaming_system.user.utils.ChannelUtils;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FollowServiceImpl implements FollowService {

    FollowRepository followRepository;
    UserRepository userRepository;
    ChannelRepository channelRepository;
    RedisTemplate<String, Long> redisTemplate;

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

        incrementFollowerCount(channelId);
    }

    @Override
    @Transactional
    public void unfollow(String userId, String channelId) {
        try {
            User user = userRepository.findById(UUID.fromString(userId)).orElseThrow(
                    () -> new AppException(ErrorCode.USER_NOT_EXIST));
            Channel channel = channelRepository.findById(Long.valueOf(channelId))
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

            boolean checkFollowed = followRepository.existsByFollowerAndChannel(user, channel);

            if (!checkFollowed)
                throw new IllegalArgumentException("User isn't follow this channel");

            this.followRepository.deleteByFollowerAndChannel(user, channel);
            decrementFollowerCount(channelId);

        } catch (Exception e) {
            log.error("Unfollow repository error: ", e.getMessage());
        }

    }

    @Override
    public boolean isFollowing(String userId, String channelId) {

        User user = userRepository.findById(UUID.fromString(userId)).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST));
        Channel channel = channelRepository.findById(Long.valueOf(channelId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        return this.followRepository.existsByFollowerAndChannel(user, channel);
    }

    @Override
    public Long getFollowerCount(String channelId) {
        return followRepository.countByChannelId(Long.valueOf(channelId));
    }

    private void incrementFollowerCount(String channelId) {
        String countFollowerKey = ChannelUtils.generateCountFollower(channelId);
        //Cache miss
        if (!redisTemplate.hasKey(countFollowerKey)) {
            Long countFollowerFromDB = followRepository.countByChannelId(Long.valueOf(channelId));
            redisTemplate.opsForValue().set(countFollowerKey, countFollowerFromDB, 1, TimeUnit.DAYS);
        }

        redisTemplate.opsForValue().increment(countFollowerKey, 1);
    }

    private void decrementFollowerCount(String channelId) {
        String countFollowerKey = ChannelUtils.generateCountFollower(channelId);
        if (!redisTemplate.hasKey(countFollowerKey)) {
            Long countFollowerFromDB = followRepository.countByChannelId(Long.valueOf(channelId));
            redisTemplate.opsForValue().set(countFollowerKey, countFollowerFromDB, 1, TimeUnit.DAYS);
        }
        redisTemplate.opsForValue().decrement(countFollowerKey, 1);
    }
}
