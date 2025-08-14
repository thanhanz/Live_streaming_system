package com.thanhan.livestreaming_system.livestream.utils;

public class StreamCacheKey {
    public static String isLivestreamingChannels() {return "isLivestreamingChannels";}
    public static String cacheConcurrencyViewers(String streamId) {
        return "live:viewer:score:" + streamId;
    }

}
