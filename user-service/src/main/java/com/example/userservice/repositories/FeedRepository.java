package com.example.userservice.repositories;

import com.example.userservice.models.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FeedRepository extends MongoRepository<Post, String> {
    List<Post> findByUserIdInOrderByPublicationDateDesc(List<String> userIds, Pageable pageable);

    List<Post> findByUserIdOrderByPublicationDateDesc(String userId, Pageable pageable);
}