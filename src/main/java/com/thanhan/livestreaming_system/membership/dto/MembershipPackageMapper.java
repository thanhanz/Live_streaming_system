package com.thanhan.livestreaming_system.membership.dto;

import com.thanhan.livestreaming_system.membership.dto.response.MembPackageResponse;
import com.thanhan.livestreaming_system.membership.entity.MembershipPackage;

public class MembershipPackageMapper {

    public static MembPackageResponse toResponse(MembershipPackage membershipPackage) {
        return new MembPackageResponse(
                membershipPackage.getId(),
                membershipPackage.getName(),
                membershipPackage.getDescription(),
                membershipPackage.getPrice(),
                membershipPackage.getDuration()
        );
    }
}
