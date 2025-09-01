package com.thanhan.livestreaming_system.comment.service;

import com.thanhan.livestreaming_system.comment.dto.request.CommentCreationRequest;
import com.thanhan.livestreaming_system.comment.dto.response.CommentResponse;
import com.thanhan.livestreaming_system.comment.entity.Comment;

import java.util.List;

public interface CommentService {

    CommentResponse createComment(CommentCreationRequest request, Long vodId);
    CommentResponse updateComment(Long commentId,  String content);
    List<CommentResponse> getRootComments(Long channelId);
    List<CommentResponse> getRepliesComments(Long parentCommentId);
    void deleteComment(Long commentId);
    Comment getCommentById(Long commentId);
    Integer countComments(Long vodId);
}