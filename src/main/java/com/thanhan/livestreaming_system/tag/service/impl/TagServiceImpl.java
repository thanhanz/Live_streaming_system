package com.thanhan.livestreaming_system.tag.service.impl;

import com.thanhan.livestreaming_system.tag.entity.Tag;
import com.thanhan.livestreaming_system.tag.repository.TagRepository;
import com.thanhan.livestreaming_system.tag.service.TagService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Override
    public Tag createTag(String name) {
        Tag existedTag = tagRepository.findByTitle(name.toLowerCase());
        if (existedTag != null) {
            return existedTag;
        }

        Tag tag = new Tag();
        tag.setTitle(name.toLowerCase());

        return tagRepository.save(tag);
    }

    @Override
    public Tag getTagByName(String tagName) {
        return tagRepository.findByTitle(tagName);
    }

    @Override
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    @Override
    public Tag getTagById(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found with id: " + id));
    }

    @Override
    public void deleteTag(Long id) {
        if (!tagRepository.existsById(id)) throw new RuntimeException("Tag not found with id: " + id);
        tagRepository.deleteById(id);
    }

}
