package com.thanhan.livestreaming_system.membership.service.impl;

import com.thanhan.livestreaming_system.membership.entity.MembershipPackage;
import com.thanhan.livestreaming_system.membership.entity.UserMembership;
import com.thanhan.livestreaming_system.membership.repository.UserMembershipRepository;
import com.thanhan.livestreaming_system.membership.service.MembershipPackageService;
import com.thanhan.livestreaming_system.membership.service.UserMembershipService;
import com.thanhan.livestreaming_system.user.entity.User;
import com.thanhan.livestreaming_system.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserMembershipServiceImpl implements UserMembershipService {

    UserMembershipRepository userMembershipRepository;

    @Override
    public UserMembership createMembership(User u, MembershipPackage pkg) {
        UserMembership membership = new UserMembership();
        membership.setUser(u);
        membership.setMembershipPackage(pkg);
        membership.setCreatedAt(LocalDateTime.now());

        int duration = pkg.getDuration();
        membership.setEndAt(membership.getCreatedAt().plusDays(duration));
        return userMembershipRepository.save(membership);
    }
}
