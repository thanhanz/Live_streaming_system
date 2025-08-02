package com.thanhan.livestreaming_system.livestream.service.impl;

import com.thanhan.livestreaming_system.livestream.service.FFmpegService;
import com.thanhan.livestreaming_system.video.dto.VodTranscodeRequest;
import com.thanhan.livestreaming_system.video.entity.Vod;
import com.thanhan.livestreaming_system.video.service.VodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class FFmpegServiceImpl implements FFmpegService {

    private final S3Client s3Client;
    private final VodService vodService;

    @Value("${cloudflare.r2.bucket}")
    private String R2Bucket;

    @Value("${cloudflare.r2.access-key}")
    private String r2AccessKey;

    @Value("${cloudflare.r2.secret-key}")
    private String r2SecretAccessKey;

    @Value("${cloudflare.r2.endpoint}")
    private String r2EndpointUrl;

    @Value("${cloudflare.r2.public-url-id}")
    private String publicR2Id;

    /*
        Thay = ten domain chu khong nen su dung Id nay`
     */
    private String PUBLIC_R2_URL = "https://" + publicR2Id + ".r2.dev/";

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
                "-b:v:1", "2000k", "-maxrate:1", "3000k", "-bufsize:1", "5000k",
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
        @Async
        public void transcodeVodToHls(VodTranscodeRequest request) throws IOException {
            log.info("Start transcode HLS VOD to R2 for VOD ID: {}", request.vodId());

            try {
                File tmpMp4File = downLoadHLSFromR2(request.vodId().toString(), request.vodStorageKey());
                if (!tmpMp4File.exists() || tmpMp4File.length() == 0) {
                    throw new RuntimeException("File not found: " + tmpMp4File.getAbsolutePath());
                }

                File parentDir = tmpMp4File.getParentFile();

                String hlsOutput = parentDir + "/%v/playlist.m3u8";
                String segmentPattern = parentDir + "/%v/segment_%03d.ts";


                List<String> ffmpegCommand = new ArrayList<>(List.of(
                        "ffmpeg",
                        "-hide_banner",
                        "-y",
                        "-i", tmpMp4File.getAbsolutePath(),

                        "-filter_complex",
//                        "[0:v]split=3[v360][v720][v1080];" +
                        "[0:v]split=2[v360][v720];" +
                                "[v360]scale=w=640:h=360:force_original_aspect_ratio=decrease[v360out];" +
                                "[v720]scale=w=1280:h=720:force_original_aspect_ratio=decrease[v720out]",
//                                "[v1080]scale=w=1920:h=1080:force_original_aspect_ratio=decrease[v1080out]",

                        // 360p
                        "-map", "[v360out]", "-map", "0:a",
                        "-c:v:0", "libx264", "-profile:v:0", "main", "-crf:0", "20", "-sc_threshold:0", "0",
                        "-g:0", "48", "-keyint_min:0", "48",
                        "-b:v:0", "800k", "-maxrate:0", "856k", "-bufsize:0", "1200k",
                        "-c:a:0", "aac", "-ar:0", "48000", "-b:a:0", "96k",

                        // Output 720p
                        "-map", "[v720out]", "-map", "0:a",
                        "-c:v:1", "libx264", "-profile:v:1", "main", "-crf:1", "20", "-sc_threshold:1", "0",
                        "-g:1", "48", "-keyint_min:1", "48",
                        "-b:v:1", "2800k", "-maxrate:1", "2996k", "-bufsize:1", "4200k",
                        "-c:a:1", "aac", "-ar:1", "48000", "-b:a:1", "128k",

                        // Output 1080p
//                        "-map", "[v1080out]", "-map", "0:a",
//                        "-c:v:2", "libx264", "-profile:v:2", "main", "-crf:2", "20", "-sc_threshold:2", "0",
//                        "-g:2", "48", "-keyint_min:2", "48",
//                        "-b:v:2", "5000k", "-maxrate:2", "5350k", "-bufsize:2", "7500k",
//                        "-c:a:2", "aac", "-ar:2", "48000", "-b:a:2", "192k",

                        // HLS Muxer Options
                        "-f", "hls",
                        "-hls_time", "4",
                        "-hls_playlist_type", "vod",
                        "-hls_flags", "independent_segments",
                        "-hls_segment_filename", segmentPattern,
                        "-master_pl_name", "master.m3u8",
                        "-var_stream_map", "v:0,a:0,name:360p v:1,a:1,name:720p ",
//                                "v:2,a:2,name:1080p",
                        hlsOutput
                ));

                log.info("Start transcode by FFmpeg command: {}", String.join(" ", ffmpegCommand));

                ProcessBuilder pb = new ProcessBuilder(ffmpegCommand);
                pb.redirectErrorStream(true);

                try {
                    Process process = pb.start();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.info("In process: {} ", line); // Bạn cần giữ dòng này để thấy lỗi cụ thể
                    }

                    int exitCode = process.waitFor();
                    if (exitCode != 0) {
                        throw new RuntimeException("FFmpeg failed with exit code " + exitCode);
                    }

                    /*
                     Upload HLS file to R2
                     */
                    uploadHLSToR2(request.vodId(), parentDir);

                    Long vodId = request.vodId();
                    String m3u8UrlInR2 = PUBLIC_R2_URL + "hls/" + vodId.toString() + "/master.m3u8";

                    Vod transcodedVod = vodService.updateVodUrl(m3u8UrlInR2, vodId);

                    log.info("Finished upload HLS file to R2 and public url: ", transcodedVod.getVideoUrl());

                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException("Failed to transcode to HLS and upload to R2 using FFmpeg: ", e);
                }

            } catch (Exception e) {
                log.error("Failed in process transcode by FFmpeg command and upload to R2: ", e);
            }
            finally {
                String localFilePath = "/tmp/vod_" + request.vodId();
                cleanTempVod(localFilePath);
            }
        }


        private File downLoadHLSFromR2(String vodId, String rawStorageKey) throws IOException {
            String tempDir = System.getProperty("java.io.tmpdir");
            String localFilePath = tempDir + File.separator + "vod_" + vodId;
            File localFile = new File(localFilePath);
            //Tao thu muc
            if (!localFile.exists()) {
                localFile.mkdirs();
            }

            String fileName = Paths.get(rawStorageKey).getFileName().toString();
            File vodFile = new File(localFilePath, fileName);
            try {
                GetObjectRequest request = GetObjectRequest.builder()
                        .key(rawStorageKey)
                        .bucket(R2Bucket)
                        .build();

                s3Client.getObject(request, ResponseTransformer.toFile(vodFile));
                return vodFile;

            } catch (Exception e) {
                log.error("Error download '{}' from R2: ", fileName, e);
                if (vodFile.exists()) {
                    vodFile.delete();
                }
                throw new IOException("Failed to download file from R2.", e);
            }
        }


    private void uploadHLSToR2(Long vodId, File hlsOutput) throws IOException {
        String baseKey = "hls/" + vodId + "/";
        log.info("Start upload file from {}  to R2!", hlsOutput.toPath());

        try (Stream<Path> paths = Files.walk(hlsOutput.toPath())) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> {
                        String relativePath = hlsOutput.toPath().relativize(path).toString();
                        if (!relativePath.endsWith(".mp4")) {
                            String key = baseKey + relativePath;
                            log.info("Upload file to R2: {}", key);
                            try {
                                String contentType = Files.probeContentType(path);
                                if (contentType == null) {
                                    if (relativePath.endsWith(".m3u8")) {
                                        contentType = "application/vnd.apple.mpegurl";
                                    } else if (relativePath.endsWith(".ts")) {
                                        contentType = "video/MP2T";
                                    }
                                }

                                log.debug("Uploading file '{}' to R2 key '{}' with Content-Type '{}'", path, key, contentType);

                                PutObjectRequest putRequest = PutObjectRequest.builder()
                                        .bucket(R2Bucket)
                                        .key(key)
                                        .contentType(contentType)
                                        .build();

                                s3Client.putObject(putRequest, RequestBody.fromFile(path.toFile()));
                            } catch (Exception e) {
                                log.error("Failed to upload file {} to R2", path, e);
                                throw new RuntimeException("Upload failed", e);
                            }
                        }
                    });
        }
    }

    private void cleanTempVod(String pathToTmpFolder) throws IOException {
        FileUtils.deleteDirectory(new File(pathToTmpFolder));
        log.info("Clean raw vod: {}", pathToTmpFolder);
    }
}
