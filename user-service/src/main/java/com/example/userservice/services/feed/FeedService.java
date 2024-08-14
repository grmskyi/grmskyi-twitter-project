package com.example.userservice.services.feed;

import com.example.userservice.models.Post;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FeedService {
    Post createPost(String userId, String content);

    List<Post> getUserFeed(String userId, Pageable pageable);

    List<Post> getSpecificUserFeed(String userId, Pageable pageable);
}