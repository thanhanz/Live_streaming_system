package com.thanhan.livestreaming_system.membership.repository;

import com.thanhan.livestreaming_system.membership.dto.request.MappingStatisticsMembership;
import com.thanhan.livestreaming_system.membership.dto.request.StatisticsMembershipGet;
import com.thanhan.livestreaming_system.membership.entity.UserMembership;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.SqlResultSetMappings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UserMembershipRepository extends JpaRepository<UserMembership, Long> {
    @Query(value =  "SELECT EXISTS (" +
            "   SELECT 1 FROM user_membership um " +
            "   WHERE um.user_id = :userId " +
            "     AND um.membership_package_id = :membershipId " +
            "     AND um.end_at >= CURRENT_TIMESTAMP" +
            " ORDER BY um.end_at DESC\n" +
            "    LIMIT 1" +
            ")", nativeQuery = true)
    Boolean checkMembership(@Param("userId") UUID userId, @Param("membershipId") Long membershipId);

    @Query(value = """
    SELECT 
        ms.membership_package_id AS packageId,
        EXTRACT(MONTH FROM ms.created_at) AS month,
        COUNT(ms.id) AS total
    FROM user_membership ms
    WHERE ms.membership_package_id IN (:pkgIds)
      AND EXTRACT(YEAR FROM ms.created_at) = :year
    GROUP BY ms.membership_package_id, month
    ORDER BY month, ms.membership_package_id
    """, nativeQuery = true)
    List<MappingStatisticsMembership> statisticMembership(@Param("pkgIds") List<Long> pkgIds, @Param("year") Integer year);
}
