package com.thanhan.livestreaming_system.tag.controller;

import com.thanhan.livestreaming_system.common.response.ApiResponse;
import com.thanhan.livestreaming_system.tag.entity.Tag;
import com.thanhan.livestreaming_system.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping
    public ApiResponse<Tag> createTag(@RequestParam String name) {
        Tag newTag = tagService.createTag(name);
        return ApiResponse.<Tag>builder()
                .data(newTag)
                .status(200)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<Tag> getTagById(@PathVariable Long id) {
        return ApiResponse.<Tag>builder()
                .data(tagService.getTagById(id))
                .status(200).build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ApiResponse.success(204, "Deleted tag with id: " + id);
    }
}
