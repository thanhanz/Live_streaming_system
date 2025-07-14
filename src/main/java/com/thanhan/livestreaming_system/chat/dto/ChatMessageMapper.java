package com.thanhan.livestreaming_system.chat.dto;

import com.thanhan.livestreaming_system.chat.entity.ChatMessage;
import com.thanhan.livestreaming_system.user.dto.mapper.UserMapper;

public class ChatMessageMapper {

    public static MessageResponse toMessageResponse(ChatMessage chatMessage) {
        return new MessageResponse(
                chatMessage.getId().toString(),
                chatMessage.getContent(),
                UserMapper.toUserResponse(chatMessage.getSender()),
                chatMessage.getStream().getId().toString(),
                chatMessage.getTimestamp());
    }

}
