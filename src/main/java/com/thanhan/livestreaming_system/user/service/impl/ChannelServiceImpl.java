package com.thanhan.livestreaming_system.user.service.impl;

import com.thanhan.livestreaming_system.common.exception.AppException;
import com.thanhan.livestreaming_system.common.exception.ErrorCode;
import com.thanhan.livestreaming_system.user.dto.mapper.ChannelMapper;
import com.thanhan.livestreaming_system.user.dto.request.ChannelCreationRequest;
import com.thanhan.livestreaming_system.user.dto.request.ChannelUpdateRequest;
import com.thanhan.livestreaming_system.user.dto.response.ChannelResponse;
import com.thanhan.livestreaming_system.user.entity.Channel;
import com.thanhan.livestreaming_system.user.entity.User;
import com.thanhan.livestreaming_system.user.repository.ChannelRepository;
import com.thanhan.livestreaming_system.user.repository.UserRepository;
import com.thanhan.livestreaming_system.user.service.ChannelService;
import com.thanhan.livestreaming_system.user.service.FollowService;
import com.thanhan.livestreaming_system.user.service.UserService;
import com.thanhan.livestreaming_system.user.utils.ChannelUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChannelServiceImpl implements ChannelService {

    ChannelRepository channelRepository;
    UserService userService;
    FollowService followService;
    RedisTemplate<String, Long> redisTemplate;

    @Override
    public ChannelResponse create(ChannelCreationRequest request) throws IllegalAccessException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User u = userService.getUserByUsername(username);

        boolean hasChannel = channelRepository.existsByOwner_Id(UUID.fromString(u.getId().toString()));

        if (hasChannel)
            throw new IllegalAccessException("User has already owned a channel!");

        Channel savedChannel = channelRepository.save(ChannelMapper.toChannel(request, u));

        return ChannelMapper.toChannelResponse(savedChannel);
    }

    @Override
    public ChannelResponse update(ChannelUpdateRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User u = userService.getUserByUsername(username);

        Channel oldChannel = channelRepository.getChannelByOwnerId(u.getId());

        oldChannel.setDescription(request.description());
        oldChannel.setDisplayName(request.displayName());
        oldChannel.setUpdatedAt(Instant.now());
//        oldChannel.setAvatarUrl(request.avatar());
//        oldChannel.setBannerUrl(request.bannerUrl());

        return ChannelMapper.toChannelResponse(channelRepository.save(oldChannel));
    }

    @Override
    public ChannelResponse getChannelById(String id) {

        Channel c = channelRepository.getChannelById(Long.valueOf(id)).orElseThrow(() ->
                new EntityNotFoundException("Channel not found!"));

        return ChannelMapper.toChannelResponse(c);
    }

    @Override
    public ChannelResponse getChannelByOwnerId(String ownerId) {
        return null;
    }

    @Override
    public Long countFollower(Long channelId) {
        String countFollowerKey = ChannelUtils.generateCountFollower(channelId.toString());
        if (!redisTemplate.hasKey(countFollowerKey)) {
            Long count = followService.getFollowerCount(channelId.toString());
            log.info("Follower count: {}", count);
            redisTemplate.opsForValue().set(countFollowerKey, count);
            return count;
        }

        return redisTemplate.opsForValue().get(countFollowerKey);
    }

    @Override
    @Transactional
    public void delete(String channelId) {
        channelRepository.deleteById(Long.valueOf(channelId));
        String countFollowerKey = ChannelUtils.generateCountFollower(channelId);
        if (redisTemplate.hasKey(countFollowerKey))
            redisTemplate.delete(countFollowerKey);
    }

    @Override
    @Transactional
    public String generateStreamKey() {
        return "";
    }

    @Override
    public Channel findById(Long channelId) {
        return channelRepository.getChannelById(channelId).orElseThrow(() -> new RuntimeException("Channel not found!"));
    }
}
