package com.thanhan.livestreaming_system.chat.controller;


import com.thanhan.livestreaming_system.chat.dto.BanChatRequest;
import com.thanhan.livestreaming_system.chat.dto.MessageResponse;
import com.thanhan.livestreaming_system.chat.entity.ChatMessage;
import com.thanhan.livestreaming_system.chat.service.ChatMessageService;
import com.thanhan.livestreaming_system.common.response.ApiResponse;
import com.thanhan.livestreaming_system.user.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatMessageController  {

    ChatMessageService chatMessageService;
    UserService userService;
    private final SimpMessagingTemplate simpMessagingTemplate;


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

    @MessageMapping("/chat/{streamId}/ban")
    public ApiResponse<Void> banChatUser(@DestinationVariable String streamId, BanChatRequest request) {
        chatMessageService.banUser(streamId, request);
        simpMessagingTemplate.convertAndSend("/livestream/topic/stream/" + streamId + "/ban", request);

        return ApiResponse.<Void>builder()
                .message("Banned chat user: " + request.userId())
                .status(202)
                .build();
    }


    @GetMapping("/stream/{streamId}/chat/banned_list")
    @ResponseBody
    public ApiResponse<Map<String, String>> getBannedUsers(@PathVariable String streamId) {
        Map<String, String> result = chatMessageService.getBannedUsers(streamId);

        return ApiResponse.<Map<String, String>>builder()
                .data(result)
                .message("Get list user banned in stream: " + streamId)
                .status(201)
                .build();
    }

    @GetMapping("/stream/{streamId}/chat/is_banned")
    @ResponseBody
    public ApiResponse<Boolean> checkIsBanned(@PathVariable String streamId, Principal principal) {
        String userId = userService.getUserIdByUsername(principal.getName());
        boolean isBanned = chatMessageService.checkIsBanned(streamId, userId);

        return ApiResponse.<Boolean>builder()
                .status(201)
                .message("Check user is banned: " + userId)
                .data(isBanned)
                .build();
    }
}
