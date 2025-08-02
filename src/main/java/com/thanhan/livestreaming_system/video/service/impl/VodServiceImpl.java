package com.thanhan.livestreaming_system.video.service.impl;

import com.thanhan.livestreaming_system.common.exception.AppException;
import com.thanhan.livestreaming_system.common.exception.ErrorCode;
import com.thanhan.livestreaming_system.livestream.service.FFmpegService;
import com.thanhan.livestreaming_system.user.entity.Channel;
import com.thanhan.livestreaming_system.user.entity.User;
import com.thanhan.livestreaming_system.user.service.ChannelService;
import com.thanhan.livestreaming_system.user.service.UserService;
import com.thanhan.livestreaming_system.video.dto.*;
import com.thanhan.livestreaming_system.video.entity.Vod;
import com.thanhan.livestreaming_system.video.messaging.producer.VideoUploadProducer;
import com.thanhan.livestreaming_system.video.repository.VodRepository;
import com.thanhan.livestreaming_system.video.service.VodService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.List;
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

    @Value("${cloudflare.r2.bucket}")
    private String R2Bucket;

    @Override
    public List<VodResponse> getVodsByChannelId(Long channelId) {
        List<Vod> vods = vodRepository.findByChannelId(channelId);

        return vods.stream().map(VodMapper::toVodResponse).collect(Collectors.toList());
    }

    @Override
    public VodResponse uploadVod(VodCreationRequest request, MultipartFile vodMp4) {
        Channel channel = channelService.findById(request.channelId());
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User authUser = userService.getUserByUsername(username);

        if (authUser.getChannel().getId() != channel.getId()) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        Vod vod = new Vod();

        vod.setTitle(request.title());
        vod.setDescription(request.description());
        vod.setImageUrl(request.imageUrl());
        vod.setChannel(channel);

        Vod savedVod = vodRepository.save(vod);

        // Upload video và sendMessage (upload success)
        String rawStorage = uploadVideoToR2(vod.getId(), vodMp4);

        videoUploadProducer.sendMessage(new VodTranscodeRequest(vod.getId(), rawStorage));
        log.info("Send message to transcode service: ", rawStorage);

        return VodMapper.toVodResponse(savedVod);
    }

    private String uploadVideoToR2(Long vodId, MultipartFile vodMp4) {
        String rawKey = "raw/vod/" + vodId + "/" + vodMp4.getName();
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(R2Bucket)
                    .key(rawKey)
                    .contentType("video/mp4")
                    .build();

            byte[] bytes = vodMp4.getBytes();
            s3Client.putObject(putRequest, RequestBody.fromBytes(bytes));

            log.info("Uploaded: {}", vodMp4.getName());
        } catch (Exception e) {
            log.error("Failed to upload file: {}", vodMp4.getName(), e);
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
        return VodMapper.toVodResponse(vod);
    }

    @Override
    public VodResponse updateVod(Long vodId, VodUpdationRequest request) {
        Vod vod = vodRepository.findById(vodId).orElseThrow(() -> new RuntimeException("Video not found"));
        vod.setTitle(request.title());
        vod.setDescription(request.description());
        vod.setImageUrl(request.imageUrl());
        vod.setOnlyMember(request.isOnlyMember());
        vod.setPublished(request.published());

        Vod updatedVod = vodRepository.save(vod);

        return VodMapper.toVodResponse(updatedVod);
    }

    @Override
    @Transactional
    public Vod updateVodUrl(String url, Long id) {
        Vod vod = vodRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Video not found"));
        vod.setVideoUrl(url);
        return vodRepository.save(vod);

    }
}
