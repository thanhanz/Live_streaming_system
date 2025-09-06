package com.thanhan.livestreaming_system.video.repository;

import com.thanhan.livestreaming_system.video.entity.Vod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VodRepository extends JpaRepository<Vod, Long> {
    Vod findById(long id);

    @Query(value = "SELECT v FROM Vod v WHERE v.channel.id = :channelId")
    Page<Vod> getPaginationByChannelId(@Param("channelId") Long channelId, Pageable pageable);

    @Query(value = "SELECT vod_id FROM vods_tags vt WHERE vt.tag_id = :tagId ORDER BY vod_id DESC", nativeQuery = true)
    List<Long> getVodIdsByTagId(@Param("tagId") Long tagId);

    @Query(value = "SELECT * FROM vods v WHERE v.category_id = :categoryId", nativeQuery = true)
    List<Vod> getVodsByCategoryId(@Param("categoryId") Long categoryId);

}
