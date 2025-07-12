package com.thanhan.livestreaming_system.chat.repository;

import com.thanhan.livestreaming_system.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
}
