package com.thanhan.livestreaming_system.livestream.repository;

import com.thanhan.livestreaming_system.livestream.entity.Stream;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StreamRepository extends JpaRepository<Stream, Long> {
    Stream findByStreamKey(String streamKey);
    Optional<Stream> findById(Long id);
}
