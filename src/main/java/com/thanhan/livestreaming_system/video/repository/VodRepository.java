package com.thanhan.livestreaming_system.video.repository;

import com.thanhan.livestreaming_system.video.entity.Vod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VodRepository extends JpaRepository<Vod, Long> {

    Vod findById(long id);
    List<Vod> findByChannelId(Long channelId);


}
