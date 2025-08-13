package com.thanhan.livestreaming_system.video.dto;

import java.util.List;

public record MultipartUploadCompleteRequest(String keyName, String uploadId, List<PartETag> parts) {
}
