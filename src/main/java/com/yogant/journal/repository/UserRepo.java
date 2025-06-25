package com.yogant.journal.repository;

import com.yogant.journal.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepo extends MongoRepository<User, Long> {
    User countUserById(Long id);

    User findByUserName(String userName);

    void deleteByUserName(String userName);
}
