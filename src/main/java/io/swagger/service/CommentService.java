package io.swagger.service;

import io.swagger.model.Comment;
import io.swagger.repository.CommentRepository;
import io.swagger.api.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CommentService {
    private static final Logger log = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
        log.info("CommentService initialized");
    }

    public Comment createComment(Comment comment) {
        log.debug("Attempting to create comment: {}", comment);

        if (comment.getContent() == null || comment.getContent().isEmpty()) {
            log.warn("Attempt to create comment with empty content");
            throw new IllegalArgumentException("Comment content cannot be empty");
        }

        try {
            Comment savedComment = commentRepository.save(comment);
            log.info("Comment created successfully with ID: {}", savedComment.getId());
            return savedComment;
        } catch (Exception e) {
            log.error("Error creating comment", e);
            throw new RuntimeException("Failed to create comment", e);
        }
    }

    public List<Comment> getAllComments() {
        log.debug("Fetching all comments");
        List<Comment> comments = commentRepository.findAll();
        log.info("Found {} comments", comments.size());
        return comments;
    }

    public Comment getCommentById(Long id) {
        log.debug("Looking for comment with ID: {}", id);
        return commentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Comment not found with ID: {}", id);
                    return new NotFoundException("Comment not found with ID: " + id);
                });
    }

    public void deleteComment(Long id) {
        log.debug("Attempting to delete comment with ID: {}", id);

        if (!commentRepository.existsById(id)) {
            log.warn("Delete failed - comment not found with ID: {}", id);
            throw new NotFoundException("Comment not found with ID: " + id);
        }

        try {
            commentRepository.deleteById(id);
            log.info("Comment deleted successfully with ID: {}", id);
        } catch (Exception e) {
            log.error("Error deleting comment with ID: {}", id, e);
            throw new RuntimeException("Failed to delete comment", e);
        }
    }
}