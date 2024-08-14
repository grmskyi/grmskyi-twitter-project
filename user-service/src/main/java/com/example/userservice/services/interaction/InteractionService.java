package com.example.userservice.services.interaction;

import com.example.userservice.models.Comment;
import com.example.userservice.models.Like;

public interface InteractionService {
    Comment addComment(String postId, String userId, String content);

    void deleteComment(String commentId, String userId);

    Like addLike(String postId, String userId);

    void removeLike(String postId, String userId);
}