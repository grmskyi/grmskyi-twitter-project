package com.example.userservice.contollers;

import com.example.userservice.models.Post;
import com.example.userservice.services.feed.FeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/feed")
@Tag(name = "User Feed", description = "APIs for retrieving and creating posts in user feeds")
public class UserFeedController {

    private final FeedService feedService;

    @Operation(summary = "Get User Feed", description = "Retrieve the feed for a user, showing posts from users they follow.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user feed",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Post.class))),
            @ApiResponse(responseCode = "400", description = "Invalid user ID or pagination information", content = @Content),
            @ApiResponse(responseCode = "404", description = "User or posts not found", content = @Content)
    })
    @GetMapping("/{userId}")
    public ResponseEntity<List<Post>> getUserFeed(@PathVariable @Valid String userId, @PageableDefault(size = 10) Pageable pageable) {
        List<Post> posts = feedService.getUserFeed(userId, pageable);
        return ResponseEntity.ok(posts);
    }

    @Operation(summary = "Get Specific User Feed", description = "Retrieve posts created by a specific user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved specific user feed",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Post.class))),
            @ApiResponse(responseCode = "400", description = "Invalid user ID or pagination information", content = @Content),
            @ApiResponse(responseCode = "404", description = "User or posts not found", content = @Content)
    })
    @GetMapping("/specific/{userId}")
    public ResponseEntity<List<Post>> getSpecificUserFeed(@PathVariable String userId, Pageable pageable) {
        List<Post> posts = feedService.getSpecificUserFeed(userId, pageable);
        return ResponseEntity.ok(posts);
    }

    @Operation(summary = "Create Post", description = "Create a new post in the specified user's feed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created a new post",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Post.class))),
            @ApiResponse(responseCode = "400", description = "Invalid user ID or post content", content = @Content)
    })
    @PostMapping("/{userId}")
    public ResponseEntity<Post> createPost(@PathVariable String userId, @RequestBody String content) {
        Post post = feedService.createPost(userId, content);
        return ResponseEntity.ok(post);
    }
}