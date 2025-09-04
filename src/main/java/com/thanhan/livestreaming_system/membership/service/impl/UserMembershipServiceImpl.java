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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserMembershipServiceImpl implements UserMembershipService {

    private static final Logger log = LoggerFactory.getLogger(UserMembershipServiceImpl.class);
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
        List<MembPackageResponse> pkgs = membershipPackageService.getPackageByChannelId(channelId);

        if (pkgs == null || pkgs.size() == 0) {
            return false;
        }

        for (MembPackageResponse pkg : pkgs) {
            if (userMembershipRepository.checkMembership(userId, pkg.id()))
                return true;
        }
        return false;
    }
}
