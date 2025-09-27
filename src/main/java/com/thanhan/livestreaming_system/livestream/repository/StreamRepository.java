package com.thanhan.livestreaming_system.livestream.repository;

import com.thanhan.livestreaming_system.livestream.dto.response.StreamStatsResponse;
import com.thanhan.livestreaming_system.livestream.entity.Stream;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
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

    @Query(value = "SELECT * FROM stream_sessions s " +
            "WHERE s.channel_id = :channelId AND s.status = 'STREAMING' " +
            "ORDER BY s.id DESC LIMIT 1", nativeQuery = true)
    Stream getCurrentLiveStreaming(@Param("channelId") Long channelId);

    @Query("SELECT COUNT(s.id) FROM Stream s WHERE s.status = 'FINISHED' ")
    Long countTotalStreams();

    @Query("SELECT new com.thanhan.livestreaming_system.livestream.dto.response.StreamStatsResponse(" +
            "MONTH(s.createdAt), COUNT(s.id)" + ") " +
            "FROM Stream s WHERE YEAR(s.createdAt) = :year AND s.status = 'FINISHED' " +
            "GROUP BY MONTH(s.createdAt) " +
            "ORDER BY MONTH(s.createdAt) ")
    List<StreamStatsResponse> statisticStreams(@Param("year")Integer year);


    @Query("SELECT s FROM Stream s WHERE s.createdAt < :createdAt AND s.status != 'PREPARING' ORDER BY s.createdAt DESC")
    List<Stream> getAllStreamPaginate(@Param("createdAt") Instant cursor, Pageable pageable);

    @Query(value = "SELECT s.* FROM stream_sessions s JOIN channels c ON s.channel_id = c.id " +
            " WHERE s.title ILIKE CONCAT('%', :query, '%') OR c.display_name ILIKE CONCAT('%', :query, '%') " +
            " ORDER BY s.channel_id DESC s.created_at DESC", nativeQuery = true)
    List<Stream> searchStream(@Param("query") String query, Pageable pageable);

    @Query("SELECT s FROM Stream s WHERE s.status = 'STREAMING' " +
            "AND s.thumbnailUrl IS NOT NULL " +
            "AND s.active = true ORDER BY s.createdAt DESC")
    List<Stream> getAllLivestreamings();
}
