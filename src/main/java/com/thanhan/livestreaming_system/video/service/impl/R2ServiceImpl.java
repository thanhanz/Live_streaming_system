package com.thanhan.livestreaming_system.video.service.impl;

import com.thanhan.livestreaming_system.video.dto.VodTranscodeRequest;
import com.thanhan.livestreaming_system.video.messaging.producer.VideoUploadProducer;
import com.thanhan.livestreaming_system.video.service.R2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class R2ServiceImpl implements R2Service {

    @Value("${cloudflare.r2.bucket}")
    private String bucketName;

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final VideoUploadProducer videoUploadProducer;

    private final String PREFIX_KEYNAME = "raw/vods/";

    @Override
    public String generatePresignedUrl(String keyName, String channelId, String contentType) {

        String locationKey = PREFIX_KEYNAME + channelId + "/" + keyName;

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(locationKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(putRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }

    @Override
    public String initMultipartFileUpload(String keyName, String channelId, String contentType) {
        String locationKey = PREFIX_KEYNAME + channelId + "/" + keyName;

        CreateMultipartUploadRequest uploadRequest = CreateMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(locationKey)
                .contentType(contentType)
                .build();

        CreateMultipartUploadResponse response = s3Client.createMultipartUpload(uploadRequest);
        return response.uploadId();
    }

    @Override
    public String generatePresignedUrlForEachPart(String keyName, String channelId, String uploadId, int partNo) {

        String locationKey = PREFIX_KEYNAME + channelId + "/" + keyName;

        UploadPartRequest partRequest = UploadPartRequest.builder()
                .bucket(bucketName)
                .key(locationKey)
                .uploadId(uploadId)
                .partNumber(partNo)
                .build();

        UploadPartPresignRequest presignRequest = UploadPartPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .uploadPartRequest(partRequest)
                .build();

        return s3Presigner.presignUploadPart(presignRequest).url().toString();
    }

    @Override
    public void completeMultipartUpload(String keyName, String channelId, String vodId,String uploadId, List<CompletedPart> completedPartList) {
        String locationKey = PREFIX_KEYNAME + channelId + "/" + keyName;

        CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
                .parts(completedPartList)
                .build();

        CompleteMultipartUploadRequest request = CompleteMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(locationKey)
                .uploadId(uploadId)
                .multipartUpload(completedMultipartUpload)
                .build();

        s3Client.completeMultipartUpload(request);
        videoUploadProducer.sendMessage(new VodTranscodeRequest(Long.valueOf(channelId), Long.valueOf(vodId),locationKey));
    }

    @Override
    public void deleteFileFromR2(String locationKey) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .key(locationKey)
                .bucket(bucketName)
                .build();

        s3Client.deleteObject(request);
    }
}
