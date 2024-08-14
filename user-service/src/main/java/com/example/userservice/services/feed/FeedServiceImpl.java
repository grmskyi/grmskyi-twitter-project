package com.example.userservice.services.feed;

import com.example.userservice.models.Post;
import com.example.userservice.models.UserSubscription;
import com.example.userservice.repositories.FeedRepository;
import com.example.userservice.repositories.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final FeedRepository feedRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;

    @Override
    public Post createPost(String userId, String content) {
        Post newPost = Post.builder()
                .userId(userId)
                .content(content)
                .publicationDate(LocalDateTime.now())
                .build();
        log.info("Creating new post: {}", newPost);
        return feedRepository.save(newPost);
    }

    @Override
    public List<Post> getUserFeed(String userId, Pageable pageable) {
        List<String> followedUserIds = userSubscriptionRepository.findByFollowerId(userId)
                .stream()
                .map(UserSubscription::getUserId)
                .collect(Collectors.toList());
        log.info("followedUserIds: {}", followedUserIds);
        return feedRepository.findByUserIdInOrderByPublicationDateDesc(followedUserIds, pageable);
    }

    @Override
    public List<Post> getSpecificUserFeed(String userId, Pageable pageable) {
        return feedRepository.findByUserIdOrderByPublicationDateDesc(userId, pageable);
    }
}