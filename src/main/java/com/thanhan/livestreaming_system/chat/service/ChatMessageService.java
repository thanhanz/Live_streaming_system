package com.thanhan.livestreaming_system.chat.service;

import com.thanhan.livestreaming_system.chat.entity.ChatMessage;

public interface ChatMessageService {

    ChatMessage saveMessage(ChatMessage chatMessage, String streamId);


}
