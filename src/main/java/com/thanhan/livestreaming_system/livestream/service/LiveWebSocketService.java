package com.thanhan.livestreaming_system.livestream.service;

public interface LiveWebSocketService {
    void sentLiveStreamStatus(String channelId, String status);
}
