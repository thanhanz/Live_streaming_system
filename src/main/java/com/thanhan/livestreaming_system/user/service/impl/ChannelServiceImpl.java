package com.thanhan.livestreaming_system.user.service.impl;

import com.thanhan.livestreaming_system.common.exception.AppException;
import com.thanhan.livestreaming_system.common.exception.ErrorCode;
import com.thanhan.livestreaming_system.livestream.service.StreamService;
import com.thanhan.livestreaming_system.user.dto.mapper.ChannelMapper;
import com.thanhan.livestreaming_system.user.dto.request.ChannelCreationRequest;
import com.thanhan.livestreaming_system.user.dto.request.ChannelUpdateRequest;
import com.thanhan.livestreaming_system.user.dto.response.ChannelAdminResponse;
import com.thanhan.livestreaming_system.user.dto.response.ChannelCacheResponse;
import com.thanhan.livestreaming_system.user.dto.response.ChannelResponse;
import com.thanhan.livestreaming_system.user.entity.Channel;
import com.thanhan.livestreaming_system.user.entity.User;
import com.thanhan.livestreaming_system.user.messaging.ChannelEventPublisher;
import com.thanhan.livestreaming_system.user.repository.ChannelRepository;
import com.thanhan.livestreaming_system.user.repository.UserRepository;
import com.thanhan.livestreaming_system.user.service.ChannelService;
import com.thanhan.livestreaming_system.user.service.FollowService;
import com.thanhan.livestreaming_system.user.service.UserService;
import com.thanhan.livestreaming_system.user.utils.ChannelUtils;
import com.thanhan.livestreaming_system.video.repository.VodRepository;
import com.thanhan.livestreaming_system.video.service.VodService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChannelServiceImpl implements ChannelService {

    final ChannelRepository channelRepository;
    final UserService userService;    final FollowService followService;
    final RedisTemplate<String, Long> redisTemplate;
    final S3Client s3Client;
    final ChannelEventPublisher eventPublisher;
    @Value("${cloudflare.r2.bucket}")
    private String R2Bucket;

    @Value("${cloudflare.r2.public-url-id}")
    private String publicR2Id;

    /*
        Thay = ten domain chu khong nen su dung Id nay`
     */
    public String getPublicR2Url() {
        return "https://" + publicR2Id + ".r2.dev/";
    }


    @Override
    @Transactional
    public ChannelResponse create(String displayName, String description, MultipartFile avatar, MultipartFile banner) throws IllegalAccessException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User u = userService.getUserByUsername(username);
        boolean hasChannel = channelRepository.existsByOwner_Id(UUID.fromString(u.getId().toString()));
        if (hasChannel)
            throw new IllegalAccessException("User has already owned a channel!");

        Channel savedChannel = channelRepository.save(ChannelMapper.toChannel(displayName, description, u));
        if (banner != null) {
            StringBuilder bannerUrl = new StringBuilder(uploadImageToR2(savedChannel.getId(), banner));
            bannerUrl = new StringBuilder(getPublicR2Url()).append(bannerUrl);
            savedChannel.setBannerUrl(bannerUrl.toString());

        }
        StringBuilder avatarUrl = new StringBuilder(uploadImageToR2(savedChannel.getId(), avatar));
        avatarUrl = new StringBuilder(getPublicR2Url()).append(avatarUrl);
        savedChannel.setAvatarUrl(avatarUrl.toString());

        Channel finalChangeChannel = channelRepository.save(savedChannel);
        eventPublisher.sendMessage(finalChangeChannel, "create");

        return ChannelMapper.toChannelResponse(finalChangeChannel);
    }

    private String uploadImageToR2(Long channelId, MultipartFile image) {
        String rawKey = "information/channels/" + channelId +"/" + System.currentTimeMillis() + "_" + image.getName();
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(R2Bucket)
                    .key(rawKey)
                    .contentType(image.getContentType())
                    .build();

            byte[] bytes = image.getBytes();
            s3Client.putObject(putRequest, RequestBody.fromBytes(bytes));
        } catch (Exception e) {
            log.error("Failed to upload file: {}", image.getName(), e);
        }
        return rawKey;
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

        Channel savedChannel = channelRepository.save(oldChannel);
        eventPublisher.sendMessage(savedChannel, "update");
        return ChannelMapper.toChannelResponse(channelRepository.save(oldChannel));
    }

    @Override
    @Transactional
    public ChannelResponse getChannelById(String id) {

        Channel c = channelRepository.getChannelById(Long.valueOf(id)).orElseThrow(() ->
                new EntityNotFoundException("Channel not found!"));

        return ChannelMapper.toChannelResponse(c);
    }

    @Override
    public Channel getChannelByOwnerId(String ownerId) {
        return channelRepository.getChannelByOwnerId(UUID.fromString(ownerId));
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
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(String channelId) {

        Channel c = channelRepository.getChannelById(Long.valueOf(channelId)).orElseThrow(() -> new EntityNotFoundException("Channel not found!"));
        channelRepository.delete(c);
        String countFollowerKey = ChannelUtils.generateCountFollower(channelId);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(countFollowerKey)))
            redisTemplate.delete(countFollowerKey);

        eventPublisher.sendMessage(c, "delete");
    }

    @Override
    @Transactional
    public List<ChannelCacheResponse> getFollowingChannels() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User u = userService.getUserByUsername(username);
        return channelRepository.getFollowingChannels(u.getId())
                .stream()
                .map(c -> ChannelMapper.toChannelCacheResponse(c, 0L)) //Tạm thời là 0 vì chưa cần đến thông tin 9 xác
                .collect(Collectors.toList());
    }

    @Override
    public Channel findById(Long channelId) {
        return channelRepository.getChannelById(channelId).orElseThrow(() -> new RuntimeException("Channel not found!"));
    }

    /**
     * For admin
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<ChannelAdminResponse> getAllChannels() {
        return channelRepository.getAllChannels();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Integer countTotalChannels() {
        return channelRepository.getTotalChannels();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<ChannelAdminResponse> searchChannels(String keyword) {
        return channelRepository.searchChannelName(keyword);
    }

    @Override
    public void banOrUnbanChannel(Long channelId) {
        Channel channel = channelRepository.getChannelById(channelId).orElseThrow(() -> new RuntimeException("Channel not found"));
        channel.setActive(!channel.getActive());
        channelRepository.save(channel);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<ChannelAdminResponse> getTop3Channels() {
        return channelRepository.getTopChannels(PageRequest.of(0, 3));
    }
}
