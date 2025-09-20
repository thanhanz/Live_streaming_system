package com.thanhan.livestreaming_system.chat.service.impl;

import com.thanhan.livestreaming_system.chat.dto.BanChatRequest;
import com.thanhan.livestreaming_system.chat.dto.ChatMessageMapper;
import com.thanhan.livestreaming_system.chat.dto.MessageResponse;
import com.thanhan.livestreaming_system.chat.entity.ChatMessage;
import com.thanhan.livestreaming_system.chat.repository.ChatMessageRepository;
import com.thanhan.livestreaming_system.chat.service.ChatMessageService;
import com.thanhan.livestreaming_system.chat.utils.ChatUtils;
import com.thanhan.livestreaming_system.common.exception.AppException;
import com.thanhan.livestreaming_system.common.exception.ErrorCode;
import com.thanhan.livestreaming_system.livestream.entity.Stream;
import com.thanhan.livestreaming_system.livestream.service.StreamService;
import com.thanhan.livestreaming_system.user.entity.User;
import com.thanhan.livestreaming_system.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatMessageServiceImpl implements ChatMessageService {

    ChatMessageRepository chatMessageRepository;
    StreamService streamService;
    UserService userService;
    RedisTemplate<String, String> redisTemplate;

    @Override
    @Transactional
    public MessageResponse saveMessage(ChatMessage chatMessage, String streamId, String username) {
        log.info("Save chat message with streamId: " + streamId);

        User u = userService.getUserByUsername(username);
        Stream stream = streamService.getStreamByStreamId(streamId);
        String listBannedUserKey = ChatUtils.bannedChatKey(streamId);

        if (redisTemplate.opsForHash().hasKey(listBannedUserKey, u.getId().toString())) {
            throw new AppException(ErrorCode.USER_BANNED_CHAT);
        }

        chatMessage.setSender(u);
        chatMessage.setStream(stream);
        chatMessage.setTimestamp(Instant.now());

        return ChatMessageMapper.toMessageResponse(chatMessageRepository.save(chatMessage));
    }

    @Override
    @Transactional
    public void banUser(String currentUserId, String streamId, BanChatRequest request) {


        if (!isOwnerOfStream(currentUserId, streamId)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        String listBannedUserKey = ChatUtils.bannedChatKey(streamId);
        redisTemplate.opsForHash().put(listBannedUserKey, request.userId(), request.username());
    }

    @Override
    @Transactional
    public Map<String, String> getBannedUsers(String userId, String streamId) {
        if (!isOwnerOfStream(userId, streamId)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        String listBannedUserKey = ChatUtils.bannedChatKey(streamId);

        if (!redisTemplate.hasKey(listBannedUserKey)) {
            log.info("No user has been banned in stream: " + streamId);
            return null;
        }

        Map<Object, Object> entries = redisTemplate.opsForHash().entries(listBannedUserKey);

        return entries.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().toString(),
                        e -> e.getValue().toString()));
    }

    @Override
    public Boolean checkIsBanned(String streamId, String userId) {
        String listBannedUserKey = ChatUtils.bannedChatKey(streamId);
        return redisTemplate.opsForHash().hasKey(listBannedUserKey, userId);
    }

    private boolean isOwnerOfStream(String userId, String streamId) {
        Stream stream = streamService.getStreamByStreamId(streamId);
        return userId.equals(stream.getChannel().getOwner().getId().toString());
    }

}
