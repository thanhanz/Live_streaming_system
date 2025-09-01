package com.thanhan.livestreaming_system.comment.service.impl;

import com.thanhan.livestreaming_system.comment.dto.mapper.CommentMapper;
import com.thanhan.livestreaming_system.comment.dto.request.CommentCreationRequest;
import com.thanhan.livestreaming_system.comment.dto.response.CommentResponse;
import com.thanhan.livestreaming_system.comment.entity.Comment;
import com.thanhan.livestreaming_system.comment.repository.CommentRepository;
import com.thanhan.livestreaming_system.comment.service.CommentService;
import com.thanhan.livestreaming_system.common.exception.AppException;
import com.thanhan.livestreaming_system.common.exception.ErrorCode;
import com.thanhan.livestreaming_system.user.entity.User;
import com.thanhan.livestreaming_system.user.service.UserService;
import com.thanhan.livestreaming_system.video.entity.Vod;
import com.thanhan.livestreaming_system.video.service.VodService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final VodService vodService;

    @Override
    @Transactional
    public CommentResponse createComment(CommentCreationRequest request, Long vodId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User u = userService.getUserByUsername(username);
        Vod video = vodService.getVodById(vodId);

        Comment comment = new Comment();
        comment.setContent(request.content());
        comment.setCreatedAt(Instant.now());
        if (request.parentCommentId() != null) {
            Comment parentComment = getCommentById(request.parentCommentId());
            comment.setParentCommentId(parentComment);
        } else
            comment.setParentCommentId(null);
        comment.setUser(u);
        comment.setVod(video);
        comment.setActive(true);
        return CommentMapper.toCommentResponse(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, String content) {
        Comment comment = getCommentById(commentId);
        comment.setContent(content);
        return CommentMapper.toCommentResponse(commentRepository.save(comment));
    }

    @Override
    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("Comment not found"));
    }

    @Override
    public List<CommentResponse> getRootComments(Long vodId) {
        return commentRepository.getRootComments(vodId).stream().map(CommentMapper::toCommentResponse).collect(Collectors.toList());
    }

    @Override
    public List<CommentResponse> getRepliesComments(Long parentCommentId) {
        return commentRepository.getRepliesComments(parentCommentId).stream().map(CommentMapper::toCommentResponse).collect(Collectors.toList()) ;
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        User user = userService.getUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        Comment comment = getCommentById(commentId);

        if (user.getId().equals(comment.getUser().getId()) || user.getId().equals(comment.getVod().getChannel().getOwner().getId()))
            commentRepository.delete(comment);
        else throw new AppException(ErrorCode.FORBIDDEN);
    }

    @Override
    public Integer countComments(Long vodId) {
        return commentRepository.getTotalComments(vodId);
    }
}
