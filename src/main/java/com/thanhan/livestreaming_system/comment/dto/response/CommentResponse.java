package com.thanhan.livestreaming_system.comment.dto.response;

import java.time.Instant;

public record CommentResponse(
        Long id,
        String content,
        Instant createdAt,
        UserCommentResponse user,
        Long parentCommentId,
        Integer countReplies,
        Long vodId
) {
}
