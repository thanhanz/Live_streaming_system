package com.thanhan.livestreaming_system.user.repository;

import com.thanhan.livestreaming_system.user.entity.Channel;
import com.thanhan.livestreaming_system.user.entity.Follow;
import com.thanhan.livestreaming_system.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, UUID> {
    boolean existsByFollowerAndChannel(User user, Channel channel);
    void deleteByFollowerAndChannel(User user, Channel channel);

}
