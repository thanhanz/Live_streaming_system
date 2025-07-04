package com.thanhan.livestreaming_system.user.service;

import java.util.UUID;

public interface FollowService {
    void follow(String userId, String channelId);
    void unfollow(String userId, String channelId);
    boolean isFollowing(String userId, String channelId);
    Long getFollowerCount(String channelId);
}
