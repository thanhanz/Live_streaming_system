package com.thanhan.livestreaming_system.comment.repository;

import com.thanhan.livestreaming_system.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query(value = "SELECT * FROM comment c WHERE c.vod_id = :vodId AND c.parent_comment_id IS NULL" +
            " ORDER BY c.created_at DESC", nativeQuery = true)
    List<Comment> getRootComments(@Param("vodId") Long vodId);

    @Query(value = "SELECT * FROM comment c WHERE c.parent_comment_id = :parentCommentId ORDER BY c.created_at DESC", nativeQuery = true)
    List<Comment> getRepliesComments(@Param("parentCommentId") Long parentCommentId);

    @Query(value = "SELECT COUNT(*) FROM comment c WHERE c.vod_id = :vodId", nativeQuery = true)
    Integer getTotalComments(Long vodId);

}
