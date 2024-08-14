package com.example.userservice.services.subscription;

import com.example.userservice.models.UserSubscription;
import com.example.userservice.pojos.UserDTO;
import com.example.userservice.repositories.UserSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private UserSubscription subscription;

    @BeforeEach
    void setUp() {
        subscription = UserSubscription.builder()
                .id("1")
                .userId("user123")
                .followerId("follower456")
                .build();
    }

    @Test
    void getFollowers_shouldReturnListOfFollowers() {
        System.out.println("Starting test: getFollowers_shouldReturnListOfFollowers");

        when(userSubscriptionRepository.findByUserId("user123"))
                .thenReturn(Collections.singletonList(subscription));

        List<UserDTO> followers = subscriptionService.getFollowers("user123");

        assertNotNull(followers);
        assertEquals(1, followers.size());
        assertEquals("follower456", followers.getFirst().getId());

        System.out.println("Followers retrieved successfully: " + followers);
    }

    @Test
    void followUser_shouldCreateNewSubscription() {
        System.out.println("Starting test: followUser_shouldCreateNewSubscription");

        when(userSubscriptionRepository.existsByUserIdAndFollowerId("user123", "follower456"))
                .thenReturn(false);

        subscriptionService.followUser("user123", "follower456");

        verify(userSubscriptionRepository).save(
                argThat(sub -> sub.getUserId().equals("user123") && sub.getFollowerId().equals("follower456"))
        );

        System.out.println("New subscription created successfully.");
    }

    @Test
    void followUser_shouldThrowExceptionWhenAlreadyFollowing() {
        System.out.println("Starting test: followUser_shouldThrowExceptionWhenAlreadyFollowing");

        when(userSubscriptionRepository.existsByUserIdAndFollowerId("user123", "follower456"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            subscriptionService.followUser("user123", "follower456");
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("User is already following user", exception.getReason());

        System.out.println("Expected exception thrown: " + exception.getMessage());
    }
}