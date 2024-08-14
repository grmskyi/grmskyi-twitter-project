package com.example.userservice.repositories;

import com.example.userservice.models.UserSubscription;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserSubscriptionRepository extends MongoRepository<UserSubscription, String> {
    List<UserSubscription> findByUserId(String userId);

    List<UserSubscription> findByFollowerId(String followerId);

    boolean existsByUserIdAndFollowerId(String userId, String followerId);
}