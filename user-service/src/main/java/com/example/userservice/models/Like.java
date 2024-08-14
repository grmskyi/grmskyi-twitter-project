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
@Document("user_likes")
public class Like {
    @Id
    private String id;

    @NotEmpty(message = "Post id cannot be empty")
    private String postId;

    @NotEmpty(message = "User id cannot be empty")
    private String userId;
}