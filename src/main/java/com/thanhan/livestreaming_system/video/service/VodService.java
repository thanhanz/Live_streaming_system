package com.thanhan.livestreaming_system.video.service;

import com.thanhan.livestreaming_system.common.paginate.PaginationResponse;
import com.thanhan.livestreaming_system.video.dto.VodCreationRequest;
import com.thanhan.livestreaming_system.video.dto.VodGetRequest;
import com.thanhan.livestreaming_system.video.dto.VodResponse;
import com.thanhan.livestreaming_system.video.dto.VodUpdationRequest;
import com.thanhan.livestreaming_system.video.entity.Vod;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.protocol.VoidSdkResponse;

import java.util.List;

public interface VodService {

    PaginationResponse<VodResponse> getAllVodsByChannelId(Long channelId, VodGetRequest request);
    String uploadVod(VodCreationRequest request, MultipartFile thumbnail);
    void deleteVod(Long id);
    VodResponse getVodById(Long id);
    VodResponse updateVod(Long id, VodUpdationRequest request);
    Vod updateVodUrl(String url, Long id);
    void hideVod(Long vodId);
    void updateViews(Long vodId, Long views);

    String initJoinVod(Long vodId, String sessionId);
    void acceptedViews(Long vodId, String sessionKey);
}