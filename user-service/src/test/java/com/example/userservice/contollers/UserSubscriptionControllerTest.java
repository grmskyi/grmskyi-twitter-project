package com.example.userservice.contollers;

import com.example.userservice.configs.RabbitMQDataUserReceiverConfig;
import com.example.userservice.pojos.UserDTO;
import com.example.userservice.services.subscription.SubscriptionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserSubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubscriptionService subscriptionService;

    @MockBean
    RabbitMQDataUserReceiverConfig rabbitMQDataUserReceiverConfig;

    @Test
    void testGetFollowers_withMultipleFollowers() throws Exception {
        System.out.println("Starting integration test: testGetFollowers_withMultipleFollowers");

        String userId = "user1";
        UserDTO follower1 = UserDTO.builder().id("follower1").build();
        UserDTO follower2 = UserDTO.builder().id("follower2").build();

        List<UserDTO> mockFollowers = Arrays.asList(follower1, follower2);

        Mockito.when(subscriptionService.getFollowers(userId)).thenReturn(mockFollowers);

        MvcResult result = mockMvc.perform(get("/api/v1/subscription/{userId}/followers", userId))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        List<UserDTO> responseFollowers = objectMapper.readValue(jsonResponse, new TypeReference<>() {
        });

        assertEquals(mockFollowers.size(), responseFollowers.size());
        assertEquals(mockFollowers.get(0).getId(), responseFollowers.get(0).getId());
        assertEquals(mockFollowers.get(1).getId(), responseFollowers.get(1).getId());
        System.out.println("Followers retrieved successfully with " + responseFollowers.size() + " users.");
    }

    @Test
    void testGetFollowers_withNoFollowers() throws Exception {
        System.out.println("Starting integration test: testGetFollowers_withNoFollowers");

        String userId = "user1";

        List<UserDTO> noFollowers = Collections.emptyList();
        Mockito.when(subscriptionService.getFollowers(userId)).thenReturn(noFollowers);

        MvcResult result = mockMvc.perform(get("/api/v1/subscription/{userId}/followers", userId))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        List<UserDTO> responseFollowers = objectMapper.readValue(jsonResponse, new TypeReference<>() {
        });

        assertTrue(responseFollowers.isEmpty());
        System.out.println("No followers retrieved successfully.");
    }


    @Test
    void testFollowUser_success() throws Exception {
        System.out.println("Starting integration test: testFollowUser_success");

        String userId = "user1";
        String followerId = "follower1";

        Mockito.doNothing().when(subscriptionService).followUser(userId, followerId);

        MvcResult result = mockMvc.perform(post("/api/v1/subscription/{userId}/follow/{followerId}", userId, followerId))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());
        System.out.println("User " + followerId + " followed user " + userId + " successfully.");
    }


    @Test
    void testFollowUser() throws Exception {
        System.out.println("Starting integration test: testFollowUser");

        String userId = "user1";
        String followerId = "follower1";

        Mockito.doNothing().when(subscriptionService).followUser(userId, followerId);

        MvcResult result = mockMvc.perform(post("/api/v1/subscription/{userId}/follow/{followerId}", userId, followerId))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());
        System.out.println("User " + followerId + " followed user " + userId + " successfully.");
    }
}