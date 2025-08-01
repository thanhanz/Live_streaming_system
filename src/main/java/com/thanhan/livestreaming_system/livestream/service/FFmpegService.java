package com.thanhan.livestreaming_system.livestream.service;

import com.thanhan.livestreaming_system.video.dto.VodTranscodeRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FFmpegService {
    
    void transcodeToHls(String streamKey);
    void transcodeVodToHls(VodTranscodeRequest request) throws IOException;
}
