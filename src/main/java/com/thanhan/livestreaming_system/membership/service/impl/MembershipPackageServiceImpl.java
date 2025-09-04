package com.thanhan.livestreaming_system.membership.service.impl;

import com.thanhan.livestreaming_system.common.exception.AppException;
import com.thanhan.livestreaming_system.membership.dto.MembershipPackageMapper;
import com.thanhan.livestreaming_system.membership.dto.request.MembPackageCreateRequest;
import com.thanhan.livestreaming_system.membership.dto.response.MembPackageResponse;
import com.thanhan.livestreaming_system.membership.entity.MembershipPackage;
import com.thanhan.livestreaming_system.membership.repository.MembershipPackageRepository;
import com.thanhan.livestreaming_system.membership.service.MembershipPackageService;
import com.thanhan.livestreaming_system.user.entity.Channel;
import com.thanhan.livestreaming_system.user.entity.User;
import com.thanhan.livestreaming_system.user.service.ChannelService;
import com.thanhan.livestreaming_system.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class MembershipPackageServiceImpl implements MembershipPackageService {

    MembershipPackageRepository membershipPackageRepository;
    ChannelService channelService;
    UserService userService;

    @Override
    public MembPackageResponse createMembershipPackage(MembPackageCreateRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User authUser = userService.getUserByUsername(username);
        Channel channel = channelService.getChannelByOwnerId(authUser.getId().toString());

        MembershipPackage membershipPackage = new MembershipPackage();
        membershipPackage.setName(request.name());
        membershipPackage.setDescription(request.description());
        membershipPackage.setPrice(request.price());
        membershipPackage.setDuration(request.duration());
        membershipPackage.setChannel(channel);

        return MembershipPackageMapper.toResponse(membershipPackageRepository.save(membershipPackage));
    }

    @Override
    public MembPackageResponse updateMembershipPackage(MembPackageCreateRequest request) {
        return null;
    }

    @Override
    @Transactional
    public void deletePackage(Long packageId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User authUser = userService.getUserByUsername(username);
        Channel channel = channelService.getChannelByOwnerId(authUser.getId().toString());
        MembershipPackage membershipPackage = membershipPackageRepository.findById(packageId)
                .orElseThrow(() -> new EntityNotFoundException("Membership package not found"));

        if (!membershipPackage.getChannel().getId().equals(channel.getId())) {
            throw new RuntimeException("Permission denied");
        }

        membershipPackageRepository.delete(membershipPackage);
    }

    @Override
    public MembPackageResponse getPackage(Long packageId) {
        MembershipPackage membershipPackage = membershipPackageRepository.findById(packageId)
                .orElseThrow(() -> new EntityNotFoundException("Membership package not found"));

        return MembershipPackageMapper.toResponse(membershipPackage);
    }


    @Override
    public MembershipPackage getPackageById(Long packageId) {
        return membershipPackageRepository.findById(packageId)
                .orElseThrow(() -> new EntityNotFoundException("Membership package not found"));
    }

    @Override
    public List<MembPackageResponse> getListMembershipPackagesByChannelId(Long channelId) {
        return membershipPackageRepository.getListMembershipPackagesByChannelId(channelId)
                .stream()
                .map(MembershipPackageMapper::toResponse).toList();
    }

    @Override
    public List<MembPackageResponse> getPackageByChannelId(Long channelId) {
        return membershipPackageRepository.findByChannelId(channelId).stream().map(MembershipPackageMapper::toResponse).toList();
    }
}
