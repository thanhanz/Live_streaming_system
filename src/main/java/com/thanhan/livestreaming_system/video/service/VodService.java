package com.thanhan.livestreaming_system.video.service;

import com.thanhan.livestreaming_system.video.dto.VodCreationRequest;
import com.thanhan.livestreaming_system.video.dto.VodResponse;
import com.thanhan.livestreaming_system.video.dto.VodUpdationRequest;
import com.thanhan.livestreaming_system.video.entity.Vod;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VodService {

    List<VodResponse> getVodsByChannelId(Long channelId);
    VodResponse uploadVod(VodCreationRequest request, MultipartFile vodMp4);
    void deleteVod(Long id);
    VodResponse getVodById(Long id);
    VodResponse updateVod(Long id, VodUpdationRequest request);
    Vod updateVodUrl(String url, Long id);


}