package io.swagger.api;

import io.swagger.model.Comment;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2025-03-26T07:34:12.780721949Z[GMT]")

@Slf4j
@RestController
public class CommentApiController implements CommentApi {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);

    private final CommentService commentService;

    public CommentApiController(CommentService commentService) {
        this.commentService = commentService;
        log.info("CommentApiController initialized");
    }

    @Override
    @GetMapping("/comment")
    public ResponseEntity<List<Comment>> getAllComments() {
        log.info("Received request to get all comments");
        List<Comment> comments = commentService.getAllComments();
        log.debug("Returning {} comments", comments.size());
        return ResponseEntity.ok(comments);
    }

    @Override
    @PostMapping("/comment")
    public ResponseEntity<Comment> createComment(@RequestBody Comment comment) {
        log.info("Received request to create new comment");
        log.debug("Comment data: {}", comment);

        Comment createdComment = commentService.createComment(comment);
        log.info("Comment created successfully with ID: {}", createdComment.getId());

        return ResponseEntity.status(201).body(createdComment);
    }

    @Override
    @GetMapping("/comment/{id}")
    public ResponseEntity<Comment> getCommentById(@PathVariable("id") Long id) {
        log.info("Received request to get comment with ID: {}", id);

        Comment comment = commentService.getCommentById(id);
        log.debug("Found comment: {}", comment);

        return ResponseEntity.ok(comment);
    }

    @Override
    @DeleteMapping("/comment/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable("id") Long id) {
        log.info("Received request to delete comment with ID: {}", id);

        commentService.deleteComment(id);
        log.info("Comment with ID {} deleted successfully", id);

        return ResponseEntity.noContent().build();
    }
}