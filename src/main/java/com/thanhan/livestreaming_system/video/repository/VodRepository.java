package com.thanhan.livestreaming_system.video.repository;

import com.thanhan.livestreaming_system.video.entity.Vod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VodRepository extends JpaRepository<Vod, Long> {
    Vod findById(long id);

    @Query(value = "SELECT * FROM Vods v WHERE v.channel_id = :channelId", nativeQuery = true)
    List<Vod> getAllByChannelId(Long channelId);

    Page<Vod> getPaginationByChannelId(Long channelId, Pageable pageable);

}
