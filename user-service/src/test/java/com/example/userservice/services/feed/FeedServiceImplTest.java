package com.example.userservice.services.feed;

import com.example.userservice.models.Post;
import com.example.userservice.models.UserSubscription;
import com.example.userservice.repositories.FeedRepository;
import com.example.userservice.repositories.UserSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedServiceImplTest {

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;

    @InjectMocks
    private FeedServiceImpl feedService;

    private Post post1, post2;
    private UserSubscription subscription1, subscription2;

    @BeforeEach
    void setUp() {
        post1 = Post.builder()
                .userId("user123")
                .content("This is the first post")
                .publicationDate(LocalDateTime.now().minusDays(1))
                .build();

        post2 = Post.builder()
                .userId("user456")
                .content("This is the second post")
                .publicationDate(LocalDateTime.now())
                .build();

        subscription1 = UserSubscription.builder()
                .userId("user456")
                .followerId("user123")
                .build();

        subscription2 = UserSubscription.builder()
                .userId("user789")
                .followerId("user123")
                .build();
    }

    @Test
    void createPost_shouldSaveAndReturnPost() {
        System.out.println("Starting test: createPost_shouldSaveAndReturnPost");

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);

        when(feedRepository.save(postCaptor.capture())).thenReturn(post1);

        Post savedPost = feedService.createPost("user123", "This is the first post");

        assertNotNull(savedPost);
        assertEquals("user123", savedPost.getUserId());
        assertEquals("This is the first post", savedPost.getContent());
        assertNotNull(savedPost.getPublicationDate());

        verify(feedRepository).save(
                argThat(p -> p.getUserId().equals("user123") && p.getContent().equals("This is the first post"))
        );

        System.out.println("Post saved and verified successfully: " + savedPost);
    }

    @Test
    void getUserFeed_shouldReturnPostsFromMultipleFollowedUsers() {
        System.out.println("Starting test: getUserFeed_shouldReturnPostsFromMultipleFollowedUsers");

        Pageable pageable = PageRequest.of(0, 10);

        when(userSubscriptionRepository.findByFollowerId("user123"))
                .thenReturn(Arrays.asList(subscription1, subscription2));
        when(feedRepository.findByUserIdInOrderByPublicationDateDesc(
                Arrays.asList("user456", "user789"), pageable))
                .thenReturn(Arrays.asList(post1, post2));

        List<Post> userFeed = feedService.getUserFeed("user123", pageable);

        assertNotNull(userFeed);
        assertEquals(2, userFeed.size());
        assertEquals("user123", userFeed.get(0).getUserId());
        assertEquals("user456", userFeed.get(1).getUserId());

        System.out.println("User feed retrieved successfully: " + userFeed);
    }

    @Test
    void getUserFeed_shouldReturnEmptyListWhenNoFollowedUsersHavePosts() {
        System.out.println("Starting test: getUserFeed_shouldReturnEmptyListWhenNoFollowedUsersHavePosts");

        Pageable pageable = PageRequest.of(0, 10);

        when(userSubscriptionRepository.findByFollowerId("user123"))
                .thenReturn(Arrays.asList(subscription1, subscription2));
        when(feedRepository.findByUserIdInOrderByPublicationDateDesc(
                Arrays.asList("user456", "user789"), pageable))
                .thenReturn(Collections.emptyList());

        List<Post> userFeed = feedService.getUserFeed("user123", pageable);

        assertNotNull(userFeed);
        assertTrue(userFeed.isEmpty());

        System.out.println("User feed retrieved as empty list when no posts found.");
    }

    @Test
    void getSpecificUserFeed_shouldReturnMultipleUserPosts() {
        System.out.println("Starting test: getSpecificUserFeed_shouldReturnMultipleUserPosts");

        Pageable pageable = PageRequest.of(0, 10);

        when(feedRepository.findByUserIdOrderByPublicationDateDesc("user123", pageable))
                .thenReturn(Arrays.asList(post1, post2));

        List<Post> specificUserFeed = feedService.getSpecificUserFeed("user123", pageable);

        assertNotNull(specificUserFeed);
        assertEquals(2, specificUserFeed.size());
        assertEquals("user123", specificUserFeed.get(0).getUserId());
        assertEquals("user456", specificUserFeed.get(1).getUserId());

        System.out.println("Specific user feed retrieved successfully with multiple posts: " + specificUserFeed);
    }

    @Test
    void getSpecificUserFeed_shouldReturnEmptyListWhenUserHasNoPosts() {
        System.out.println("Starting test: getSpecificUserFeed_shouldReturnEmptyListWhenUserHasNoPosts");

        Pageable pageable = PageRequest.of(0, 10);

        when(feedRepository.findByUserIdOrderByPublicationDateDesc("user123", pageable))
                .thenReturn(Collections.emptyList());

        List<Post> specificUserFeed = feedService.getSpecificUserFeed("user123", pageable);

        assertNotNull(specificUserFeed);
        assertTrue(specificUserFeed.isEmpty());

        System.out.println("Specific user feed retrieved as empty list when no posts found.");
    }
}