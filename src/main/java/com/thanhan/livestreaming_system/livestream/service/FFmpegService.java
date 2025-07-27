package com.thanhan.livestreaming_system.livestream.service;

public interface FFmpegService {
    
    void transcodeToHls(String streamKey);
}
