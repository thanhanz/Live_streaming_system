package com.thanhan.livestreaming_system.video.utils;

public class VodsRedisKey {

    public static String acceptedViewKey(String vodId) {
        return "view:" + vodId + ":views_pending";
    }
}
