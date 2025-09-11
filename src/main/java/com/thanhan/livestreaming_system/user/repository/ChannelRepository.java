package com.thanhan.livestreaming_system.user.repository;

import com.thanhan.livestreaming_system.user.dto.response.ChannelAdminResponse;
import com.thanhan.livestreaming_system.user.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChannelRepository extends JpaRepository<Channel, Long> {

    boolean existsByOwner_Id(UUID ownerId);

    @Query("SELECT c FROM Channel c WHERE c.owner.id = :ownerId")
    Channel getChannelByOwnerId(@Param("ownerId") UUID ownerId);

    Optional<Channel> getChannelById(Long id);

    @Query("SELECT c FROM Channel c " +
            "JOIN Follow f ON f.channel.id = c.id " +
            "WHERE f.follower.id = :userId ")
    List<Channel> getFollowingChannels(@Param("userId")UUID userId);


    @Query("SELECT new com.thanhan.livestreaming_system.user.dto.response.ChannelAdminResponse( " +
            "c.id, " +
            "c.displayName, " +
            "c.owner.username, " +
            "c.followersCount, " +
            "COUNT(DISTINCT v.id), " +
            "COUNT(DISTINCT s.id)) " +
            "FROM Channel c " +
            "LEFT JOIN c.vods v " +
            "LEFT JOIN c.streams s " +
            "WHERE LOWER(c.displayName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "AND (s.status IS NULL OR s.status = 'FINISHED') " +
            "GROUP BY c.id, c.displayName, c.owner.username, c.followersCount")
    List<ChannelAdminResponse> searchChannelName(@Param("query") String query);

    @Query(value = "SELECT COUNT(c.id) FROM channels c", nativeQuery = true)
    Integer getTotalChannels();

    @Query("SELECT new com.thanhan.livestreaming_system.user.dto.response.ChannelAdminResponse( " +
            "c.id, " +
            "c.displayName, " +
            "c.owner.username, " +
            "c.followersCount, " +
            "COUNT(DISTINCT v.id), " +
            "COUNT(DISTINCT s.id)) " +
            "FROM Channel c " +
            "LEFT JOIN c.vods v " +
            "LEFT JOIN c.streams s WITH s.status = 'FINISHED' " +
            "GROUP BY c.id, c.displayName, c.owner.username, c.followersCount " +
            "ORDER BY c.followersCount DESC ")
    List<ChannelAdminResponse> getAllChannels();
}
