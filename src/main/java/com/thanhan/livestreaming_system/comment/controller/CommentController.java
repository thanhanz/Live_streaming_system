package com.thanhan.livestreaming_system.comment.controller;


import com.thanhan.livestreaming_system.comment.dto.request.CommentCreationRequest;
import com.thanhan.livestreaming_system.comment.dto.response.CommentResponse;
import com.thanhan.livestreaming_system.comment.entity.Comment;
import com.thanhan.livestreaming_system.comment.service.CommentService;
import com.thanhan.livestreaming_system.common.response.ApiResponse;
import com.thanhan.livestreaming_system.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    @GetMapping("/vod/{vodId}/root_comments")
    public ApiResponse<List<CommentResponse>> getRootComments(@PathVariable("vodId") Long vodId) {
        return ApiResponse.<List<CommentResponse>>builder()
                .data(commentService.getRootComments(vodId))
                .status(201)
                .message("List all root comment in: " + vodId)
                .build();
    }

    @GetMapping("/root_comments/{commentId}")
    public ApiResponse<List<CommentResponse>> getRepliesComments(@PathVariable("commentId") Long parentCommentId) {
        return ApiResponse.<List<CommentResponse>>builder()
                .data(commentService.getRepliesComments(parentCommentId))
                .status(201)
                .message("List all root comment in: " + parentCommentId)
                .build();
    }

    @PostMapping("/vod/{vodId}")
    public ApiResponse<CommentResponse> createComment(@PathVariable("vodId") Long vodId, @RequestBody CommentCreationRequest request) {
        CommentResponse comment = commentService.createComment(request, vodId);
        return ApiResponse.<CommentResponse>builder()
                .data(comment)
                .status(200)
                .message("Create comment success")
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<CommentResponse> updateComment(@PathVariable("id") Long commentId, @RequestBody String content) {
        return ApiResponse.<CommentResponse>builder()
                .data(commentService.updateComment(commentId, content))
                .status(202)
                .message("Update comment success").build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteComment(@PathVariable("id") Long commentId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String userId = userService.getUserIdByUsername(username);
        commentService.deleteComment(commentId, userId);
        return ApiResponse.success(200, "Delete comment success");
    }

    @GetMapping("/vod/{vodId}")
    public ApiResponse<Integer> countComments(@PathVariable("vodId") Long vodId) {
        return ApiResponse.<Integer>builder()
                .status(200)
                .message("Get total comment in video success")
                .data(commentService.countComments(vodId))
                .build();
    }

}
