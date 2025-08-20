package com.thanhan.livestreaming_system.livestream.repository;

import com.thanhan.livestreaming_system.livestream.entity.Stream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StreamRepository extends JpaRepository<Stream, Long> {
    Stream findByStreamKey(String streamKey);
    Optional<Stream> findById(Long id);
    List<Stream> findByChannelId(Long channelId);

    @Query(value =  "SELECT * FROM stream_sessions s " +
                    "WHERE s.channel_id = :channelId  AND s.status = 'FINISHED' " +
                    "ORDER BY s.created_at DESC", nativeQuery = true)
    List<Stream> findFinishedStreamByChannelId(@Param("channelId") Long channelId);

}
