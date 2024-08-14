package com.example.userservice.services.subscription;


import com.example.userservice.models.UserSubscription;
import com.example.userservice.pojos.UserDTO;
import com.example.userservice.repositories.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final UserSubscriptionRepository userSubscriptionRepository;

    @Override
    public void getDataFromMQ(UserDTO userDTO) {
        UserDTO
                .builder()
                .id(userDTO.getId())
                .build();
    }

    @Override
    public List<UserDTO> getFollowers(String userId) {
        List<UserSubscription> subscriptions = userSubscriptionRepository.findByUserId(userId);

        return subscriptions.stream()
                .map(sub -> new UserDTO(sub.getFollowerId()))
                .collect(Collectors.toList());
    }

    @Override
    public void followUser(String userId, String followerId) {
        if (userSubscriptionRepository.existsByUserIdAndFollowerId(userId, followerId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already following user");
        }

        UserSubscription subscription = UserSubscription.builder()
                .userId(userId)
                .followerId(followerId)
                .build();
        userSubscriptionRepository.save(subscription);
        log.info("New follow relationship created: {} -> {}", followerId, userId);
    }
}