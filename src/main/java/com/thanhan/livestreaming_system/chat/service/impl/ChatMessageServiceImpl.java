package com.thanhan.livestreaming_system.chat.service.impl;

import com.thanhan.livestreaming_system.chat.dto.ChatMessageMapper;
import com.thanhan.livestreaming_system.chat.dto.MessageResponse;
import com.thanhan.livestreaming_system.chat.entity.ChatMessage;
import com.thanhan.livestreaming_system.chat.repository.ChatMessageRepository;
import com.thanhan.livestreaming_system.chat.service.ChatMessageService;
import com.thanhan.livestreaming_system.livestream.entity.Stream;
import com.thanhan.livestreaming_system.livestream.service.StreamService;
import com.thanhan.livestreaming_system.user.entity.User;
import com.thanhan.livestreaming_system.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.bridge.Message;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatMessageServiceImpl implements ChatMessageService {

    ChatMessageRepository chatMessageRepository;
    StreamService streamService;
    UserService userService;

    @Override
    @Transactional
    public MessageResponse saveMessage(ChatMessage chatMessage, String streamId, String username) {
        log.info("Save chat message with streamId: " + streamId);

        User u = userService.getUserByUsername(username);
        Stream stream = streamService.getStreamByStreamId(streamId);

        chatMessage.setSender(u);
        chatMessage.setStream(stream);
        chatMessage.setTimestamp(Instant.now());

        return ChatMessageMapper.toMessageResponse(chatMessageRepository.save(chatMessage));
    }
}
