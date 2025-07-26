package com.thanhan.livestreaming_system.livestream.service.impl;

import com.thanhan.livestreaming_system.livestream.service.FFmpegService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

        String inputUrl = "rtmp://rtmp-server:1935/live/" + streamKey;
        String outputDir = "/var/www/html/hls/" + streamKey;
        new File(outputDir).mkdirs();

        String hlsOutput = outputDir + "/%v/playlist.m3u8";
        String segmentPattern = outputDir + "/%v/segment_%03d.ts";
        String record_livestream = outputDir + "/recording_%03d.mp4";
        int recordingTime = 1800; //seconds = 30'
        List<String> liveCommand = new ArrayList<> (List.of(
                "ffmpeg",
                "-i", inputUrl,
                "-y",

                "-c:v", "copy", "-c:a", "copy", "-f", "segment",
                "-segment_time", String.valueOf(recordingTime),
                "-segment_format", "mp4",
                record_livestream,

                "-filter_complex",
                "[0:v]split=3[v360][v720][v1080];" +
                        "[v360]scale=640:360[v360_scaled];" +
                        "[v720]scale=1280:720[v720_scaled];" +
                        "[v1080]scale=1920:1080[v1080_scaled]",

                "-map", "[v360_scaled]", "-map", "[v720_scaled]", "-map", "[v1080_scaled]",
                "-map", "0:a", "-map", "0:a", "-map", "0:a",

                // 360p
                "-c:v:0", "libx264", "-preset", "veryfast", "-profile:v", "main", "-level", "3.0",
                "-g", "60", "-keyint_min", "60", "-sc_threshold", "0",
                "-b:v:0", "800k", "-maxrate:0", "1000k", "-bufsize:0", "1600k",
                "-s:v:0", "640x360",
                "-c:a:0", "aac", "-b:a:0", "96k", "-ac:0", "2",

                // 720p
                "-c:v:1", "libx264", "-preset", "veryfast", "-profile:v", "main", "-level", "3.1",
                "-g", "60", "-keyint_min", "60", "-sc_threshold", "0",
                "-b:v:1", "2500k", "-maxrate:1", "3000k", "-bufsize:1", "5000k",
                "-s:v:1", "1280x720",
                "-c:a:1", "aac", "-b:a:1", "128k", "-ac:1", "2",

                // 1080p
                "-c:v:2", "libx264", "-preset", "veryfast", "-profile:v", "high", "-level", "4.0",
                "-g", "60", "-keyint_min", "60", "-sc_threshold", "0",
                "-b:v:2", "5000k", "-maxrate:2", "6000k", "-bufsize:2", "10000k",
                "-s:v:2", "1920x1080",
                "-c:a:2", "aac", "-b:a:2", "160k", "-ac:2", "2",

                "-f", "hls",
                "-hls_time", "3",
                "-hls_list_size", "6",
                "-hls_flags", "delete_segments+independent_segments",
                "-master_pl_name", "master.m3u8",
                "-hls_segment_filename", segmentPattern,
                "-var_stream_map", "v:0,a:0,name:360p v:1,a:1,name:720p v:2,a:2,name:1080p",
                hlsOutput
        ));

        ProcessBuilder livePb = new ProcessBuilder(liveCommand);
        livePb.redirectErrorStream(true);

        try {
            livePb.start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to livestream using FFmpeg: ", e);
        }
    }

    @Override
    public void startRecording(String streamKey) {
        String inputUrl = "rtmp://rtmp-server:1935/live/" + streamKey;
        String outputDir = "/var/www/html/hls/" + streamKey;
        String recordOutput = outputDir + "/record_" + System.currentTimeMillis() + ".mp4";

        List<String> recordCommand = new ArrayList<>(List.of(
                "ffmpeg",
                "-i", inputUrl,
                "-y",
                "-c:v", "libx264",
                "-preset", "medium",
                "-crf", "20",
                "-c:a", "aac",
                "-b:a", "160k",
                "-f", "mp4",
                "-movflags", "frag_keyframe+empty_moov",
                recordOutput
        ));

        ProcessBuilder recordPb = new ProcessBuilder(recordCommand);
        recordPb.redirectErrorStream(true);

        try {
            recordPb.start();
        } catch (IOException e) {
            throw new RuntimeException("Error recording livestream: ", e);
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
