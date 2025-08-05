package com.thanhan.livestreaming_system.membership.service;

import com.thanhan.livestreaming_system.membership.dto.request.MembPackageCreateRequest;
import com.thanhan.livestreaming_system.membership.dto.response.MembPackageResponse;
import com.thanhan.livestreaming_system.membership.entity.MembershipPackage;

public interface MembershipPackageService {
    //CRUD
    MembPackageResponse createMembershipPackage(MembPackageCreateRequest request);
    MembPackageResponse updateMembershipPackage(MembPackageCreateRequest request);
    void deletePackage(Long packageId);
    MembPackageResponse getPackage(Long packageId);
    MembershipPackage getPackageById(Long packageId);

}
