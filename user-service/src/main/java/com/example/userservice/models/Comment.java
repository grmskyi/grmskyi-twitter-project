package com.example.userservice.models;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_comments")
public class Comment {
    @Id
    private String id;

    @NotEmpty(message = "Post id cannot be empty")
    private String postId;

    @NotEmpty(message = "User id cannot be empty")
    private String userId;

    @NotEmpty(message = "Content cannot be empty")
    private String content;

    @NotEmpty(message = "Publication date cannot be empty")
    private LocalDateTime publicationDate;
}