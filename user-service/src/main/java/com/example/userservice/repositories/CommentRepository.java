package com.example.userservice.repositories;

import com.example.userservice.models.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommentRepository extends MongoRepository<Comment, String> {
    void deleteByIdAndUserId(String commentId, String userId);
}