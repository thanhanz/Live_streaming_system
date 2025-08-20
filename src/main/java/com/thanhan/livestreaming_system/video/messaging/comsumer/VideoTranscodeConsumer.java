package com.thanhan.livestreaming_system.video.messaging.comsumer;

import com.thanhan.livestreaming_system.livestream.service.FFmpegService;
import com.thanhan.livestreaming_system.video.dto.VodTranscodeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class VideoTranscodeConsumer {

    private final FFmpegService ffmpegService;

    @RabbitListener(queues = "${rabbitmq.transcode.vod.queue}")
    public void receiveMessage(VodTranscodeRequest request) throws IOException {
        ffmpegService.transcodeVodToHls(request);
    }
}
