package com.grmskyi.main.repositories;

import com.grmskyi.main.models.UserCredentials;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserCredentialsRepository extends MongoRepository<UserCredentials, String> {
    UserCredentials findByEmail(String email);
}