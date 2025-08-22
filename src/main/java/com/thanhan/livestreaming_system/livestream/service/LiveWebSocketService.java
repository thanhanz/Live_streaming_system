package com.thanhan.livestreaming_system.livestream.service;

public interface LiveWebSocketService {
    void sentLiveStreamStatus(Long channelId, String status);
}
