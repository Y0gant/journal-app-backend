package com.y0gant.springDemo.repository;

import com.y0gant.springDemo.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepo extends MongoRepository<User, Long> {
    User countUserById(Long id);
}
