package com.thanhan.livestreaming_system.chat.service;

import com.thanhan.livestreaming_system.chat.dto.MessageResponse;
import com.thanhan.livestreaming_system.chat.entity.ChatMessage;

public interface ChatMessageService {

    MessageResponse saveMessage(ChatMessage chatMessage, String streamId, String username);


}
