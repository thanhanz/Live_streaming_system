package com.thanhan.livestreaming_system.comment.dto.request;

public record CommentCreationRequest(String content, Long parentCommentId) {

}
