package com.thanhan.livestreaming_system.livestream.utils;

public class StreamCacheKey {

    public static String isWatchingKey(String streamId) {
        return "isWatchingKey:" + streamId;
    }

}
