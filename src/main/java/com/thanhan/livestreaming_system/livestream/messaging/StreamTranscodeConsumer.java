package com.thanhan.livestreaming_system.livestream.messaging;

import com.thanhan.livestreaming_system.livestream.service.FFmpegService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class StreamTranscodeConsumer {

    private final FFmpegService ffmpegService;

    @RabbitListener(queues = "${rabbitmq.transcode.live.queue}")
    public void receiveLiveMessage(String streamKey) throws IOException {
        ffmpegService.transcodeToHls(streamKey);
//        ffmpegService.recordingVideo(streamKey);
    }
}
