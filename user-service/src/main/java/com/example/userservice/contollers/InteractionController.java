package com.example.userservice.contollers;

import com.example.userservice.models.Comment;
import com.example.userservice.models.Like;
import com.example.userservice.services.interaction.InteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/interactions")
@Tag(name = "User Interactions", description = "APIs for commenting and liking posts")
public class InteractionController {
    private final InteractionService interactionService;

    @Operation(summary = "Post a Comment", description = "Post a comment on a specific post.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully posted a comment",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Comment.class))),
            @ApiResponse(responseCode = "400", description = "Invalid post ID or user ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "Post or user not found", content = @Content)
    })
    @PostMapping("/comment/{postId}")
    public ResponseEntity<Comment> postComment(@PathVariable @Valid String postId,
                                               @RequestParam @Valid String userId,
                                               @RequestBody @Valid String content) {
        Comment comment = interactionService.addComment(postId, userId, content);
        return ResponseEntity.ok(comment);
    }

    @Operation(summary = "Delete a Comment", description = "Delete a comment by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted the comment"),
            @ApiResponse(responseCode = "400", description = "Invalid comment ID or user ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "Comment not found", content = @Content)
    })
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable @Valid String commentId,
                                              @RequestParam @Valid String userId) {
        interactionService.deleteComment(commentId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Add a Like", description = "Add a like to a specific post.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully added a like",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Like.class))),
            @ApiResponse(responseCode = "400", description = "User already liked this post", content = @Content)
    })
    @PostMapping("/like/{postId}")
    public ResponseEntity<Like> addLike(@PathVariable @Valid String postId,
                                        @RequestParam @Valid String userId) {
        Like like = interactionService.addLike(postId, userId);
        return ResponseEntity.ok(like);
    }

    @Operation(summary = "Remove a Like", description = "Remove a like from a specific post.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully removed the like"),
            @ApiResponse(responseCode = "400", description = "Invalid post ID or user ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "Like not found", content = @Content)
    })
    @DeleteMapping("/like/{postId}")
    public ResponseEntity<Void> removeLike(@PathVariable @Valid String postId,
                                           @RequestParam @Valid String userId) {
        interactionService.removeLike(postId, userId);
        return ResponseEntity.ok().build();
    }
}