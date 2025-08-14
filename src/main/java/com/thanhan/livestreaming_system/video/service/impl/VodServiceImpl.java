package com.thanhan.livestreaming_system.video.service.impl;

import com.thanhan.livestreaming_system.common.exception.AppException;
import com.thanhan.livestreaming_system.common.exception.ErrorCode;
import com.thanhan.livestreaming_system.common.paginate.PaginationResponse;
import com.thanhan.livestreaming_system.livestream.service.FFmpegService;
import com.thanhan.livestreaming_system.user.dto.mapper.ChannelMapper;
import com.thanhan.livestreaming_system.user.dto.response.ChannelCacheResponse;
import com.thanhan.livestreaming_system.user.entity.Channel;
import com.thanhan.livestreaming_system.user.entity.User;
import com.thanhan.livestreaming_system.user.service.ChannelService;
import com.thanhan.livestreaming_system.user.service.UserService;
import com.thanhan.livestreaming_system.video.dto.*;
import com.thanhan.livestreaming_system.video.entity.Vod;
import com.thanhan.livestreaming_system.video.messaging.producer.VideoUploadProducer;
import com.thanhan.livestreaming_system.video.repository.VodRepository;
import com.thanhan.livestreaming_system.video.service.VodService;
import com.thanhan.livestreaming_system.video.utils.VodsRedisKey;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VodServiceImpl implements VodService {

    private final VodRepository vodRepository;
    private final ChannelService channelService;
    private final UserService userService;
    private final S3Client s3Client;
    private final VideoUploadProducer videoUploadProducer;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${cloudflare.r2.bucket}")
    private String R2Bucket;

    @Override
    public PaginationResponse<VodResponse> getAllVodsByChannelId(Long channelId, VodGetRequest request) {
        Sort.Direction direction = Sort.Direction.fromOptionalString(request.getOrder()).orElse(Sort.Direction.DESC);
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "createdAt";

        Channel c = channelService.findById(channelId);
        ChannelCacheResponse channelResponse = new ChannelCacheResponse(c.getId().toString(), c.getDisplayName(), c.getAvatarUrl(), c.getFollowersCount().longValue());

        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getLimit(), Sort.by(direction, sortBy));

        Page<Vod> pageResult = vodRepository.getPaginationByChannelId(channelId, pageable);

        List<Vod> items = new ArrayList<>(pageResult.getContent());
        List<VodResponse> result = items.stream().map(vod -> {
            String pendingViewKey = VodsRedisKey.acceptedViewKey(vod.getId().toString());
            Object pending = redisTemplate.opsForValue().get(pendingViewKey);
            Long pendingView = 0L;

            if (pending != null) {
                if (pending instanceof Number) {
                    pendingView = ((Number) pending).longValue();
                } else if (pending instanceof String) {
                    pendingView = Long.parseLong((String) pending);
                }
            }

            Long view = Boolean.TRUE.equals(redisTemplate.hasKey(pendingViewKey))
                    ? vod.getTotalView() +  pendingView
                    : vod.getTotalView();

            return VodMapper.toVodResponse(vod, view, channelResponse);
        }).toList();

        return PaginationResponse.<VodResponse>builder()
                .page(pageResult.getNumber() + 1)
                .limit(pageResult.getSize())
                .totalItems((int) pageResult.getTotalElements())
                .totalPage(pageResult.getTotalPages())
                .items(result)
                .build();
    }

    @Override
    public String uploadVod(VodCreationRequest request, MultipartFile thumbnail) {
        Channel channel = channelService.findById(request.channelId());
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User authUser = userService.getUserByUsername(username);

        if (authUser.getChannel().getId() != channel.getId()) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        Vod vod = new Vod();
        vod.setTitle(request.title());
        vod.setDescription(request.description());
        vod.setChannel(channel);
        vod.setTotalView(0L);
        String storageThumbnail = uploadThumbnailToR2(thumbnail);

        /*
            Set prefix to cache thumbnail from R2
         */
        vod.setThumbnail(storageThumbnail);
        Vod savedVod = vodRepository.save(vod);


//        videoUploadProducer.sendMessage(new VodTranscodeRequest(vod.getId(), rawStorage));
//        log.info("Send message to transcode service: ", rawStorage);

        return savedVod.getId().toString();
    }

    private String uploadThumbnailToR2(MultipartFile thumbnail) {
        String rawKey = "vods/thumbnail/" + thumbnail.getName();
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(R2Bucket)
                    .key(rawKey)
                    .contentType(thumbnail.getContentType())
                    .build();

            byte[] bytes = thumbnail.getBytes();
            s3Client.putObject(putRequest, RequestBody.fromBytes(bytes));

            log.info("Uploaded: {}", thumbnail.getName());
        } catch (Exception e) {
            log.error("Failed to upload file: {}", thumbnail.getName(), e);
        }
        return rawKey;
    }


    @Override
    public void deleteVod(Long id) {
        Vod vod = vodRepository.findById(id).orElseThrow(() -> new RuntimeException("Video not found"));
        vodRepository.delete(vod);
    }

    @Override
    public VodResponse getVodById(Long id) {
        Vod vod = vodRepository.findById(id).orElseThrow(() -> new RuntimeException("Video not found"));
        ChannelCacheResponse response = getChannelCache(vod);
        String pendingViewKey = VodsRedisKey.acceptedViewKey(vod.getId().toString());

        Long view = redisTemplate.hasKey(pendingViewKey)
                ? Long.valueOf(vod.getTotalView() + redisTemplate.opsForValue().get(pendingViewKey))
                : vod.getTotalView();

        return VodMapper.toVodResponse(vod, view, response);
    }

    @Override
    public VodResponse updateVod(Long vodId, VodUpdationRequest request) {
        Vod vod = vodRepository.findById(vodId).orElseThrow(() -> new RuntimeException("Video not found"));
        vod.setTitle(request.title());
        vod.setDescription(request.description());
        vod.setThumbnail(request.imageUrl());
        vod.setOnlyMember(request.isOnlyMember());
        vod.setPublished(request.published());
        Vod updatedVod = vodRepository.save(vod);

        ChannelCacheResponse response = getChannelCache(vod);
        String pendingViewKey = VodsRedisKey.acceptedViewKey(vod.getId().toString());

        Long view = redisTemplate.hasKey(pendingViewKey)
                ? Long.valueOf(vod.getTotalView() + redisTemplate.opsForValue().get(pendingViewKey))
                : vod.getTotalView();

        return VodMapper.toVodResponse(updatedVod, view, response);
    }

    @Override
    public void updateViews(Long vodId, Long views) {
        Vod vod = vodRepository.findById(vodId).orElseThrow(() -> new RuntimeException("Video not found"));
        vod.setTotalView(vod.getTotalView() + views);
        vodRepository.save(vod);
    }

    @Override
    @Transactional
    public Vod updateVodUrl(String url, Long id) {
        Vod vod = vodRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Video not found"));
        vod.setVideoUrl(url);
        return vodRepository.save(vod);
    }

    @Override
    public void hideVod(Long vodId) {
        Vod vod = vodRepository.findById(vodId).orElseThrow(() -> new EntityNotFoundException("Video not found"));
        vod.setOnlyMember(false);
        vodRepository.save(vod);
    }

    private ChannelCacheResponse getChannelCache(Vod vod) {
        Long totalFollowers = channelService.countFollower(vod.getChannel().getId());
        return ChannelMapper.toChannelCacheResponse(vod.getChannel(), totalFollowers);
    }

    @Override
    public String initJoinVod(Long vodId, String sessionId) {
        String sessionKey = "vods:" + vodId + ":" + sessionId;
        redisTemplate.opsForValue().setIfAbsent(sessionKey, sessionId, 65, TimeUnit.SECONDS);

        return sessionKey;
    }

    @Override
    public void acceptedViews(Long vodId, String sessionKey) {
        Boolean isAccepted = redisTemplate.hasKey(sessionKey);
        String pendingKey = VodsRedisKey.acceptedViewKey(vodId.toString());

        if (Boolean.TRUE.equals(isAccepted)) {
            redisTemplate.opsForValue().increment(pendingKey, 1);
            redisTemplate.delete(sessionKey);
        }
    }
}
