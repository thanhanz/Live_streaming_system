package com.thanhan.livestreaming_system.user.repository;

import com.thanhan.livestreaming_system.user.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ChannelRepository extends JpaRepository<Channel, Long> {

    boolean existsByOwner_Id(UUID ownerId);

    @Query("SELECT c FROM Channel c WHERE c.owner.id = :ownerId")
    Channel getChannelByOwnerId(@Param("ownerId") UUID ownerId);

    Optional<Channel> getChannelById(Long id);
}
