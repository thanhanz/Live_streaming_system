package com.thanhan.livestreaming_system.membership.service.impl;

import com.thanhan.livestreaming_system.membership.dto.response.MembPackageResponse;
import com.thanhan.livestreaming_system.membership.entity.MembershipPackage;
import com.thanhan.livestreaming_system.membership.entity.UserMembership;
import com.thanhan.livestreaming_system.membership.repository.UserMembershipRepository;
import com.thanhan.livestreaming_system.membership.service.MembershipPackageService;
import com.thanhan.livestreaming_system.membership.service.UserMembershipService;
import com.thanhan.livestreaming_system.user.entity.User;
import com.thanhan.livestreaming_system.user.service.ChannelService;
import com.thanhan.livestreaming_system.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserMembershipServiceImpl implements UserMembershipService {

    UserMembershipRepository userMembershipRepository;
    MembershipPackageService membershipPackageService;

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

    @Override
    public Boolean checkMembership(Long channelId, UUID userId) {
        MembPackageResponse pkg = membershipPackageService.getPackageByChannelId(channelId);
        if (pkg != null)
            return userMembershipRepository.checkMembership(userId, pkg.id()).isPresent();
        else return true;
    }
}
