package com.thanhan.livestreaming_system.membership.service;

import com.thanhan.livestreaming_system.membership.entity.MembershipPackage;
import com.thanhan.livestreaming_system.membership.entity.UserMembership;
import com.thanhan.livestreaming_system.user.entity.User;

import java.util.UUID;

public interface UserMembershipService {
    UserMembership createMembership(User user, MembershipPackage membershipPackage);
    Boolean checkMembership(Long channelId, UUID userId);
}
