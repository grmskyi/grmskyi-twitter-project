package com.example.userservice.services.subscription;

import com.example.userservice.pojos.UserDTO;

import java.util.List;

public interface SubscriptionService {
    void getDataFromMQ(UserDTO userDTO);

    List<UserDTO> getFollowers(String userId);

    void followUser(String userId, String followerId);
}