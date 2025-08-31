package com.thanhan.livestreaming_system.membership.repository;

import com.thanhan.livestreaming_system.membership.entity.MembershipPackage;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MembershipPackageRepository extends JpaRepository<MembershipPackage, Long> {

    @Query(value = "SELECT * FROM membership_package pkg WHERE pkg.channel_id = :channelId", nativeQuery = true)
    List<MembershipPackage> getListMembershipPackagesByChannelId(@Param("channelId") Long channelId);

    MembershipPackage findByChannelId(@Param("channelId") Long channelId);
}
