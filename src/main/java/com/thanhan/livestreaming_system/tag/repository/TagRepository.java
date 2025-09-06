package com.thanhan.livestreaming_system.tag.repository;

import com.thanhan.livestreaming_system.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Tag findByTitle(String title);
}
