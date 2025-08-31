package com.thanhan.livestreaming_system.membership.repository;

import com.thanhan.livestreaming_system.membership.entity.UserMembership;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserMembershipRepository extends JpaRepository<UserMembership, Long> {
    @Query(value =  "SELECT EXISTS (" +
            "   SELECT 1 FROM user_membership um " +
            "   WHERE um.user_id = :userId " +
            "     AND um.membership_package_id = :membershipId " +
            "     AND um.end_at >= CURRENT_TIMESTAMP" + ")",
            nativeQuery = true)
    Optional<Integer> checkMembership(@Param("userId") UUID userId, @Param("membershipId") Long membershipId);
}
