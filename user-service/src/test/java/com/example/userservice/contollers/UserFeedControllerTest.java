package com.example.userservice.contollers;

import com.example.userservice.configs.RabbitMQDataUserReceiverConfig;
import com.example.userservice.models.Post;
import com.example.userservice.services.feed.FeedService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserFeedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FeedService feedService;

    @MockBean
    RabbitMQDataUserReceiverConfig rabbitMQDataUserReceiverConfig;

    @Test
    void testGetUserFeed() throws Exception {
        System.out.println("Starting integration test: testGetUserFeed");

        String userId = "user1";
        Pageable pageable = PageRequest.of(0, 10);

        Post post1 = Post.builder()
                .id("post1")
                .userId(userId)
                .content("First post content")
                .publicationDate(LocalDateTime.now())
                .build();

        Post post2 = Post.builder()
                .id("post2")
                .userId(userId)
                .content("Second post content")
                .publicationDate(LocalDateTime.now())
                .build();

        List<Post> mockPosts = Arrays.asList(post1, post2);

        Mockito.when(feedService.getUserFeed(userId, pageable)).thenReturn(mockPosts);

        MvcResult result = mockMvc.perform(get("/api/v1/feed/{userId}", userId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        List<Post> responsePosts = objectMapper.readValue(jsonResponse, new TypeReference<>() {
        });

        assertEquals(mockPosts.size(), responsePosts.size());
        assertEquals(mockPosts.get(0).getId(), responsePosts.get(0).getId());
        assertEquals(mockPosts.get(1).getId(), responsePosts.get(1).getId());
        System.out.println("User feed retrieved successfully with " + responsePosts.size() + " posts.");
    }

    @Test
    void testGetSpecificUserFeed() throws Exception {
        System.out.println("Starting integration test: testGetSpecificUserFeed");

        String userId = "user1";
        Pageable pageable = PageRequest.of(0, 10);

        Post post1 = Post.builder()
                .id("post1")
                .userId(userId)
                .content("Specific user's first post content")
                .publicationDate(LocalDateTime.now())
                .build();

        Post post2 = Post.builder()
                .id("post2")
                .userId(userId)
                .content("Specific user's second post content")
                .publicationDate(LocalDateTime.now())
                .build();

        List<Post> mockPosts = Arrays.asList(post1, post2);

        Mockito.when(feedService.getSpecificUserFeed(userId, pageable)).thenReturn(mockPosts);

        MvcResult result = mockMvc.perform(get("/api/v1/feed/specific/{userId}", userId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        List<Post> responsePosts = objectMapper.readValue(jsonResponse, new TypeReference<>() {
        });

        assertEquals(mockPosts.size(), responsePosts.size());
        assertEquals(mockPosts.get(0).getId(), responsePosts.get(0).getId());
        assertEquals(mockPosts.get(1).getId(), responsePosts.get(1).getId());
        System.out.println("Specific user feed retrieved successfully with " + responsePosts.size() + " posts.");
    }

    @Test
    void testCreatePost() throws Exception {
        System.out.println("Starting integration test: testCreatePost");

        String userId = "user1";
        String content = "New post content";

        Post mockPost = Post.builder()
                .id("post1")
                .userId(userId)
                .content(content)
                .publicationDate(LocalDateTime.now())
                .build();

        Mockito.when(feedService.createPost(userId, content)).thenReturn(mockPost);

        MvcResult result = mockMvc.perform(post("/api/v1/feed/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        Post responsePost = objectMapper.readValue(jsonResponse, Post.class);

        assertEquals(mockPost.getId(), responsePost.getId());
        assertEquals(mockPost.getUserId(), responsePost.getUserId());
        assertEquals(mockPost.getContent(), responsePost.getContent());
        System.out.println("Post created successfully: " + responsePost.getId());
    }
}