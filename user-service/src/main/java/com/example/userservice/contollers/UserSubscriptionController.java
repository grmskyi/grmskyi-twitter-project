package com.example.userservice.contollers;


import com.example.userservice.pojos.UserDTO;
import com.example.userservice.services.subscription.SubscriptionService;
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

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/subscription")
@Tag(name = "User Subscription", description = "APIs for managing user subscriptions and followers")
public class UserSubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "Get Followers", description = "Retrieve the list of users who are following the specified user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of followers",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid user ID supplied", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<UserDTO>> getFollowers(@PathVariable @Valid String userId) {
        List<UserDTO> followers = subscriptionService.getFollowers(userId);
        return ResponseEntity.ok(followers);
    }

    @Operation(summary = "Follow User", description = "Create a subscription for the specified user to follow another user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully followed the user"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID or follower ID supplied", content = @Content),
            @ApiResponse(responseCode = "409", description = "User is already following the specified user", content = @Content)
    })
    @PostMapping("/{userId}/follow/{followerId}")
    public ResponseEntity<Void> followUser(@PathVariable @Valid String userId,
                                           @PathVariable @Valid String followerId) {
        subscriptionService.followUser(userId, followerId);
        return ResponseEntity.ok().build();
    }
}