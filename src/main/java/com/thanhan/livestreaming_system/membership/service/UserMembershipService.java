package com.thanhan.livestreaming_system.membership.service;

import com.thanhan.livestreaming_system.membership.entity.MembershipPackage;
import com.thanhan.livestreaming_system.membership.entity.UserMembership;
import com.thanhan.livestreaming_system.user.entity.User;

public interface UserMembershipService {
    UserMembership createMembership(User user, MembershipPackage membershipPackage);
}
