package com.thanhan.livestreaming_system.video.service;

import software.amazon.awssdk.services.s3.model.CompletedPart;

import java.util.List;

public interface R2Service {
    String generatePresignedUrl(String keyName, String channelId, String contentType);

    String initMultipartFileUpload(String keyName, String channelId, String contentType);
    String generatePresignedUrlForEachPart(String keyName, String channelId, String uploadId, int partNo);
    void completeMultipartUpload(String keyName, String channelId, String vodId, String uploadId, List<CompletedPart> completedPartList);

    void deleteFileFromR2(String locationKey);
}
