package com.thanhan.livestreaming_system.chat.service;

import com.thanhan.livestreaming_system.chat.dto.BanChatRequest;
import com.thanhan.livestreaming_system.chat.dto.MessageResponse;
import com.thanhan.livestreaming_system.chat.entity.ChatMessage;

import java.util.Map;

public interface ChatMessageService {

    MessageResponse saveMessage(ChatMessage chatMessage, String streamId, String username);
    void banUser(String currentUserId, String streamId, BanChatRequest request);
    Map<String, String> getBannedUsers(String userId, String streamId);
    Boolean checkIsBanned(String streamId, String userId);
}
