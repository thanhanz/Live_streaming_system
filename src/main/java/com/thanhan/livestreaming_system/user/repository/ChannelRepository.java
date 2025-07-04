package com.thanhan.livestreaming_system.user.repository;

import com.thanhan.livestreaming_system.user.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelRepository extends JpaRepository<Channel, Long> {
}
