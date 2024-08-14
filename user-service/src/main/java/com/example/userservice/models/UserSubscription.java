package com.example.userservice.models;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("user_subscriptions")
public class UserSubscription {

    @Id
    private String id;

    @NotEmpty(message = "Follower id cannot be empty")
    private String followerId;

    @NotEmpty(message = "User id cannot be empty")
    private String userId;
}