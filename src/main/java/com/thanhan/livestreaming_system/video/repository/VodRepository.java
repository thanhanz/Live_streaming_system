package com.thanhan.livestreaming_system.video.repository;

import com.thanhan.livestreaming_system.video.entity.Vod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface VodRepository extends JpaRepository<Vod, Long> {
    Vod findById(long id);

    @Query("SELECT v FROM Vod v WHERE v.channel.id = :channelId " +
            "AND v.createdAt < :cursor " +
            "ORDER BY v.createdAt DESC")
    List<Vod> getVodsByChannelId(@Param("channelId") Long channelId, @Param("cursor") Instant cursor, Pageable pageable);

    @Query(value = "SELECT vod_id FROM vods_tags vt WHERE vt.tag_id = :tagId ORDER BY vod_id DESC", nativeQuery = true)
    List<Long> getVodIdsByTagId(@Param("tagId") Long tagId);

    @Query(value = "SELECT * FROM vods v WHERE v.category_id = :categoryId", nativeQuery = true)
    List<Vod> getVodsByCategoryId(@Param("categoryId") Long categoryId);

    @Query(value = "SELECT * FROM vods v WHERE v.views < :views ORDER BY v.views DESC ", nativeQuery = true)
    List<Vod> findNextPageByViewsDesc(@Param("views") Long views, Pageable pageable);

    @Query(value = "SELECT * FROM vods v WHERE v.views < :views ORDER BY v.views ASC ", nativeQuery = true)
    List<Vod> findNextPageByViewsAsc(@Param("views") Long views, Pageable pageable);

    @Query(value = "SELECT * FROM vods v WHERE v.created_at < :createdAt ORDER BY v.created_at DESC ", nativeQuery = true)
    List<Vod> findNextPageByCreatedAtDesc(@Param("createdAt") Instant createdAt, Pageable pageable);

    @Query(value = "SELECT * FROM vods v WHERE v.created_at < :createdAt ORDER BY v.created_at ASC ", nativeQuery = true)
    List<Vod> findNextPageByCreatedAtAsc(@Param("createdAt") Instant createdAt, Pageable pageable);

    @Query(value = "SELECT v.* FROM vods v JOIN channels c ON c.id = v.channel_id " +
            "WHERE v.title ILIKE CONCAT('%', :query, '%') OR c.display_name ILIKE CONCAT('%', :query, '%') " +
            " ORDER BY v.channel_id DESC", nativeQuery = true)
    List<Vod> searchVodsByChannelNameOrTitle(@Param("query") String query);

    @Query(value = "SELECT v.id FROM Vod v")
    Integer countAll();
}