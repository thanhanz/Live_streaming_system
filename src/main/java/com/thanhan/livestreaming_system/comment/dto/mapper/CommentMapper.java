package com.thanhan.livestreaming_system.comment.dto.mapper;

import com.thanhan.livestreaming_system.comment.dto.response.CommentResponse;
import com.thanhan.livestreaming_system.comment.dto.response.UserCommentResponse;
import com.thanhan.livestreaming_system.comment.entity.Comment;

import java.util.List;
import java.util.Optional;

public class CommentMapper {

    public static CommentResponse toCommentResponse(Comment comment) {
        int repliesCount = Optional.ofNullable(comment.getReplies())
                .map(List::size)
                .orElse(0);

        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                new UserCommentResponse(comment.getUser().getUsername(), comment.getUser().getAvatar()),
                comment.getParentCommentId() == null ? null : comment.getParentCommentId().getId(),
                repliesCount,
                comment.getVod().getId()
        );
    }

}
