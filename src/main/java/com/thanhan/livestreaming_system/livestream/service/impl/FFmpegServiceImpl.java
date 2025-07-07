package com.thanhan.livestreaming_system.livestream.service.impl;

import com.thanhan.livestreaming_system.livestream.service.FFmpegService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class FFmpegServiceImpl implements FFmpegService {


    @Override
    @Async
    public void startTranscode(String streamKey) {

        log.info("Start transcode with streamKey: " + streamKey);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String inputUrl = "rtmp://rtmp-server:1935/live/" + streamKey;
        String outputDir = "/var/www/html/hls/" + streamKey;
        String outputUrl = outputDir + "/index.m3u8";

        // Tạo thư mục nếu chưa có
        new File(outputDir).mkdirs();

        List<String> command = List.of(
                "ffmpeg",
                "-i", inputUrl,
                "-c:v", "libx264", "-preset", "veryfast", "-b:v", "2500k",
                "-c:a", "aac", "-b:a", "160k", "-ac", "2",
                "-f", "hls",
                "-hls_time", "4",
                "-hls_list_size", "10",
                "-hls_flags", "delete_segments",
                outputUrl
        );

        log.info("URL output: " + outputUrl);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true); // merge stderr into stdout

        try {
            pb.start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to start FFmpeg", e);
        }
    }
}
