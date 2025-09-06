package com.thanhan.livestreaming_system.tag.dto;

import com.thanhan.livestreaming_system.tag.entity.Tag;

public class TagMapper {

    public static TagResponse toTagResponse(Tag tag) {
        return new TagResponse(tag.getId(), tag.getTitle());
    }
}
