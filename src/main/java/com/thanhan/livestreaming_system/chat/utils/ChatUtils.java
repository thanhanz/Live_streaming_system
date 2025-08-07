package com.thanhan.livestreaming_system.chat.utils;

public class ChatUtils {

    public static String bannedChatKey(String streamId) {
        return "chat:banned:" + streamId;
    }
}
