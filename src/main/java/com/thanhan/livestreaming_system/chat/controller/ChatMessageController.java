package com.thanhan.livestreaming_system.chat.controller;


import com.thanhan.livestreaming_system.chat.entity.ChatMessage;
import com.thanhan.livestreaming_system.chat.service.ChatMessageService;
import com.thanhan.livestreaming_system.common.response.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatMessageController  {

    ChatMessageService chatMessageService;

    @MessageMapping("/chat/{streamId}/send")
    @SendTo("/topic/stream/{streamId}")
    public ApiResponse<ChatMessage> sendMessage(@PathVariable String streamId,
                                   @Payload ChatMessage chatMessage) {

        return ApiResponse.<ChatMessage>builder()
                .status(201)
                .message("Send message success!")
                .data(chatMessageService.saveMessage(chatMessage, streamId)).build();
    }

}
