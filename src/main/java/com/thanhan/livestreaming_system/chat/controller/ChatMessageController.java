package com.thanhan.livestreaming_system.chat.controller;


import com.thanhan.livestreaming_system.chat.dto.MessageResponse;
import com.thanhan.livestreaming_system.chat.entity.ChatMessage;
import com.thanhan.livestreaming_system.chat.service.ChatMessageService;
import com.thanhan.livestreaming_system.common.response.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatMessageController  {

    ChatMessageService chatMessageService;

    @MessageMapping("/chat/{streamId}/send")
    @SendTo("/livestream/topic/stream/{streamId}")
    public ApiResponse<MessageResponse> sendMessage(@DestinationVariable String streamId, //PathVariable chi su dung cho Http (RequestMapping)
                                                    @Payload ChatMessage chatMessage, Principal principal) {
        String username = principal.getName();
        log.info("Sending chat message {}", chatMessage);
        return ApiResponse.<MessageResponse>builder()
                .status(201)
                .message("Send message success!")
                .data(chatMessageService.saveMessage(chatMessage, streamId, username)).build();
    }

}
