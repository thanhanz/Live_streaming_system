package com.thanhan.livestreaming_system.tag.service;

import com.thanhan.livestreaming_system.tag.entity.Tag;

import java.util.List;

public interface TagService {
    Tag createTag(String name);
    List<Tag> getAllTags();
    Tag getTagById(Long id);
    void deleteTag(Long id);
    Tag getTagByName(String tagName);
}
