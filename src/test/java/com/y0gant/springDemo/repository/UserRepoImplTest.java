package com.y0gant.springDemo.repository;

import org.bson.assertions.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserRepoImplTest {

    @Autowired
    private UserRepoImpl userRepo;

    @Test
    void getUsersWithSA() {
        Assertions.assertNotNull(userRepo.getUsersWithSA());
    }
}