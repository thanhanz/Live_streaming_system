package com.thanhan.livestreaming_system.user.utils;

public class ChannelUtils {

    public static String generateCountFollower(String channelId) {
        return "channel:" + channelId +":follower";
    }

}
