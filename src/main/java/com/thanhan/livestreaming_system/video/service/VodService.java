package com.thanhan.livestreaming_system.video.service;

import com.thanhan.livestreaming_system.common.paginate.PaginationResponse;
import com.thanhan.livestreaming_system.tag.dto.TagRequest;
import com.thanhan.livestreaming_system.video.dto.*;
import com.thanhan.livestreaming_system.video.entity.Vod;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.protocol.VoidSdkResponse;

import java.util.List;

public interface VodService {

    String uploadMetadataForVod(VodCreationRequest request, MultipartFile thumbnail);
    void deleteVod(Long id);
    VodResponse getVodResById(Long id);
    Vod getVodById(Long id);
    VodResponse updateVod(Long id, VodUpdationRequest request);
    Vod updateVodUrl(String url, Long id);
    void hideVod(Long vodId);
    void updateViews(Long vodId, Long views);
    String initJoinVod(Long vodId, String sessionId);
    void acceptedViews(Long vodId, String sessionKey);

    void assignCategory(Long vodId, Long categoryId);
    void addTags(Long vodId, TagRequest request);
    List<VodResponse> getVodsByTagName(String tagName);
    List<VodResponse> getVodsByCategoryId(Long categoryId);

    /**
     * Admin service
     */
    PaginationResponse<VodResponse> getAllVodsByChannelId(Long channelId, VodGetRequest request);
    PaginationResponse<VodAdminResponse> getAllVods( VodGetRequest request);
    List<VodAdminResponse> searchVodsByTitleOrChannelName(String query);
    Integer countTotalVods();
}