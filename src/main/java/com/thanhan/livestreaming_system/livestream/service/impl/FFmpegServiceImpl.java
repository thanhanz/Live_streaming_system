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
    public void transcodeToHls(String streamKey) {

        log.info("Start transcode HLS: " + streamKey);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        //Input file .mp4 from upload func
        String inputUrl = "rtmp://rtmp-server:1935/live/" + streamKey;

        String outputDir = "/var/www/html/hls/" + streamKey;
        String outputUrl = outputDir + "/index.m3u8";

        // Tạo thư mục nếu chưa có
        new File(outputDir).mkdirs();

        //Upload can phai co 480p, 720p, 1080p
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

    @Override
    public void transcodeToDash(String streamKey) {
        log.info("Start transcode to Dash: " + streamKey);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String inputUrl = "rtmp://rtmp-server:1935/live/" + streamKey;
        String outputDir = "/var/www/html/dash/" + streamKey;

        //Create folder if not exist
        new File(outputDir).mkdirs();

        String outputUrl = outputDir + "/main_stream.mpd";
        //Upload can phai co 480p, 720p, 1080p



        List<String> command = List.of(
                "ffmpeg",
                "-i", inputUrl,

                "-filter_complex",
                "[0:v]split=3[v1080][v720][v480];" +
                        "[v1080]scale=1920:1080[v1080out];" +
                        "[v720]scale=1280:720[v720out];" +
                        "[v480]scale=854:480[v480out]",

                "-map", "[v1080out]", "-b:v:0", "5000k",
                "-map", "[v720out]",  "-b:v:1", "2000k",
                "-map", "[v480out]",  "-b:v:2", "1000k",
                "-map", "0:a",         "-b:a",   "128k",

                "-c:v", "libx264", "-preset", "veryfast", "-crf", "23",
                "-c:a", "aac",

                "-f", "dash",
                "-use_timeline", "1",
                "-use_template", "1",
                "-window_size",  "5",
                "-seg_duration", "4",

                "-init_seg_name", "init-$RepresentationID$.mp4",
                "-media_seg_name", "chunk-$RepresentationID$-$Number$.m4s",
                "-adaptation_sets", "id=0,streams=v id=1,streams=a",

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
